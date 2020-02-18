exports.predict_v2 = predict_v2;

/* Predicts survival based on patient input parameters.
   Returns an array of: [bcSpecSur, cumOverallSurOL, cumOverallSurChemo, cumOverallSurHormo, cumOverallSurCandH, cumOverallSurCHT,
   pySurv10OL, pySurv10Chemo, pySurv10Hormo, pySurv10CandH, pySurv10CHT]
   The first 5 are survival vars at a specfic no. of years as input
   The remaining 4 are float values, in person years.

   Arguments age, size and nodes are entered as values; the others as lookups
   # This is how the model assigns some input parameters (or ranges) into variables
   # i.e. parameter (or ranges) -> web form setting -> Predict model variable setting
   # Tumour Grade (1,2,3,unknown) -> (1,2,3,9) -> (1.0,2.0,3.0,2.13)
   # ER Status (-ve,+ve) -> (0,1) -> (0,1) n.b. unknown not allowed
   # Detection (Clinical,Screening,Other) -> (0,1,2) -> (0.0,1.0,0.204)
   # Chemo (1st,2nd,3rd) -> (1,2,3) -> (1,2,3)
   # HER2 Status (-ve,+ve,unknown) -> (0,1,9) -> (0,1,9) n.b. these are now changed in web code (were (1,2,0))
   # KI67 Status (-ve,+ve,unknown) -> (0,1,9) -> (0,1,9) n.b. these are now changed in web code (were (1,2,0))
*/
function predict_v2(age, size, nodes, grade, erstat, detection, chemoGen, her2, ki67, rtime) {

   // first decode input settings for grade, detection
   var detection_values = [0.0, 1.0, 0.204]; // inputs are (0,1,2)
   detection = detection_values[detection];
   var grade_values = [1.0, 2.0, 3.0, 2.13]; // inputs are (1,2,3,9)
   if (grade == 9) grade = 4;
   grade = grade_values[grade - 1];
   // prevent high hazard for young patients - age of 24.54 caps Hazard ratio at 4.008
   if (age < 25) {
     age = 25
   };
   var grade_a = 0;
   if (grade == 2 || grade == 3) {
       grade_a = 1;
   }
   //console.log("check grade,detection: ",grade,detection);
   /* n.b. default of 0 for her2_rh and ki67_rh remains when inputs are set as undefined */
   var her2_rh = 0;
   if (her2 == 1 && erstat == 1) {
       her2_rh = 0.2413;
   }
   if (her2 == 0 && erstat == 1) {
       her2_rh = -0.0762;
   }
   if (her2 == 1 && erstat == 0) {
       her2_rh = 0.2413;
   }
   if (her2 == 0 && erstat == 0) {
       her2_rh = -0.0762;
   }
   var ki67_rh = 0;
   if (ki67 == 1 && erstat == 1) {
       ki67_rh = 0.14904;
   }
   if (ki67 == 0 && erstat == 1) {
       ki67_rh = -0.11333;
   }

   // Note:
   // For uncertainties in the coefficients h,c,t etc, see docs/Predictv2-uncertainties.docx

   var c = 0;
   // No chemo - use initial value of c i.e. 0
   // First chemoGen - not used here for completeness
   var c_low;
   var c_high;
   if (chemoGen == 1) {
       if (age < 50 && erstat == 0) {
           c = -0.3567;
       }
       if (age >= 50 && age < 60 && erstat == 0) {
           c = -0.2485;
       }
       if (age >= 60 && erstat == 0) {
           c = -0.1278;
       }
       if (age < 50 && erstat == 1) {
           c = -0.3567;
       }
       if (age >= 50 && age < 60 && erstat == 1) {
           c = -0.1744;
       }
       if (age >= 60 && erstat == 1) {
           c = -0.0834;
       }
   }
   // Second chemoGen
   if (chemoGen == 2) {
       c_low = -0.360;
       c = -0.248;
       c_high = -0.136;
   }
   // Third chemoGen
   if (chemoGen == 3) {
       c_low = -0.579;
       c = -0.446;
       c_high = -0.313
   }
   var surv_oth_time = [];
   var surv_oth_year = [];
   var bs = [];
   surv_oth_time[0] = 0;
   surv_oth_year[0] = 0;
   bs[0] = 0;
   // The number of columns is not important, as it is not required to specify the size of an array before using it
   var ftime = Math.round(rtime);
   var mort_rate_cum_rx = create2DArray(19, ftime + 1);
   var surv_br_time_rx = create2DArray(19, ftime + 1);
   var pr_all_time_rx = create2DArray(19, ftime + 1);
   var pr_oth_time_rx = create2DArray(19, ftime + 1);
   var pr_br_time_rx = create2DArray(19, ftime + 1);
   var pr_dfs_time_rx = create2DArray(19, ftime + 1);
   var benefit_h, benefit_h10, benefit_c, benefit_t, benefit_hc, benefit_h10c, benefit_hct, benefit_h10ct;
   var benefit_h_low, benefit_h_high, benefit_c_low, benefit_c_high, benefit_t_low, benefit_t_high;
   var benefit_hc_low, benefit_hc_high, benefit_hct_low, benefit_hct_high;
   var time;
   // initialise results_time with entry for time = 0. Later code starts at time = 1.
   var results_time = [{
                          // outputs required for predict bar chart display
                          "bcSpecSur": 1.0,
                          "cumOverallSurOL": 1.0,
                          "cumOverallSurHormo": 0,
                          "cumOverallSurChemo": 0,
                          "cumOverallSurCandH": 0,
                          "cumOverallSurCHT": 0,
                          //"br": 0, // breast cancer related deaths
                          "oth": 0, // other cause of death
                          "marginSurHormo": [0, 0],
                          "marginSurChemo": [0, 0],
                          "marginSurCandH": [0, 0],
                          "marginSurCHT": [0, 0]
                       }]
   for (time = 1; time <= ftime; ++time) {
       var pi;
       // Calculate the breast cancer mortality prognostic index (pi)
       // Generate baseline survival
       if (erstat == 1) {
           pi = 34.53642 * (Math.pow(age / 10.0, -2.0) - 0.0287449295) -
               34.20342 * (Math.pow(age / 10.0, -2.0) * Math.log(age / 10.0) - 0.0510121013) +
               0.7530729 * (Math.log(size / 100.0) + 1.545233938) +
               0.7060723 * (Math.log((nodes + 1) / 10.0) + 1.387566896) +
               0.746655 * grade -
               0.22763366 * detection +
               her2_rh + ki67_rh;
           bs[time] = Math.exp(0.7424402 - 7.527762 * (Math.pow(1.0 / time, 0.5)) - 1.812513 * Math.pow(1.0 / time, 0.5) * Math.log(time));
       }
       if (erstat == 0) {
           pi = 0.0089827 * (age - 56.3254902) +
               2.093446 * (Math.pow(size / 100.0, 0.5) - 0.5090456276) +
               0.6260541 * (Math.log((nodes + 1) / 10.0) + 1.086916249) +
               1.129091 * grade_a +
               her2_rh + ki67_rh;
           bs[time] = Math.exp(-1.156036 + 0.4707332 / Math.pow(time, 2.0) - 3.51355 / time);
       }
       // Generate therapy reduction coefficients
       var h = 0;
       var h_low;
       var h_high;


       var t = 0;
       var t_low;
       var t_high;

       if (erstat == 1) {
           h_low = -0.502;
           h = -0.3857;
           h_high = -0.212;
       }

       var h10 = h;
       var h10_low;
       var h10_high;

       if (erstat == 1 && time > 10) {
           h10_low = h_low - 0.3425;
           h10 = h - 0.3425;
           h10_high = h_high - 0.3425;
       } // Relative risk 0.71 years 11-15
       if (her2 == 1) {
           t_low = -0.533;
           t = -0.3567;
           t_high = -0.239;
       }
       // console.log("(her2_rh,ki67_rh,pi,bs,h,c,t):",her2_rh,ki67_rh,pi,bs,h,c,t);
       var hc = h + c;
       var hct = h + c + t;
       var h10c = h10 + c;
       var h10ct = h10 + c + t;
       var hc_low = h + c_low;
       var hc_high = h + c_high;
       var hct_low = h + c + t_low;
       var hct_high = h + c + t_high;

       /* this indexing is pretty gross, but hey ho, let's not rock the boat */
       var types = [0, h, h10, c, t, hc, h10c, hct, h10ct, h_low, h_high, c_low, c_high, t_low, t_high, hc_low, hc_high, hct_low, hct_high];

       // Generate cumulative survival non-breast mortality
       var bs_oth = Math.exp(-6.052919 + 1.079863 * Math.log(time) + 0.3255321 * Math.pow(time, 0.5));
       surv_oth_time[time] = Math.exp(-Math.exp(0.0698252 * (Math.pow(age / 10.0, 2.0) - 34.23391957)) * bs_oth);
       // Generate annual survival from cumulative survival
       if (time == 1) {
           surv_oth_year[time] = 1 - surv_oth_time[time];
       }
       if (time > 1) {
           surv_oth_year[time] = surv_oth_time[time - 1] - surv_oth_time[time];
       }
       var mort_rate_rx, surv_br_year_rx, pr_all_year_rx, proportion_br_rx, pr_oth_year_rx, pr_br_year_rx;
       var pr_oth_time_0;
       var pr_all_time_0;
       var pr_br_time_0;
       var pr_dfs_time_0;
       for (var i = 0; i < types.length; i++) {
           var rx = types[i];
           //* Generate the breast cancer specific survival
           if (time == 1) {
               mort_rate_rx = bs[time] * Math.exp(pi + rx);
           }
           if (time > 1) {
               mort_rate_rx = (bs[time] - bs[time - 1]) * Math.exp(pi + rx);
           }
           if (time == 1) {
               mort_rate_cum_rx[i][time] = mort_rate_rx;
           }
           if (time > 1) {
               mort_rate_cum_rx[i][time] = mort_rate_rx + mort_rate_cum_rx[i][time - 1];
           }
           surv_br_time_rx[i][time] = Math.exp(-mort_rate_cum_rx[i][time])
           if (time == 1) {
               surv_br_year_rx = 1 - surv_br_time_rx[i][time];
           }
           if (time > 1) {
               surv_br_year_rx = surv_br_time_rx[i][time - 1] - surv_br_time_rx[i][time];
           }
           //* All cause mortality
           pr_all_time_rx[i][time] = 1 - surv_oth_time[time] * surv_br_time_rx[i][time]; // Cumulative all cause mortality
           if (time == 1) {
               pr_all_year_rx = pr_all_time_rx[i][time];
           } // Number deaths in year 1
           if (time > 1) {
               pr_all_year_rx = pr_all_time_rx[i][time] - pr_all_time_rx[i][time - 1];
           }
           //* Proportion of all cause mortality
           proportion_br_rx = (surv_br_year_rx) / (surv_oth_year[time] + surv_br_year_rx)
           pr_oth_year_rx = (1 - proportion_br_rx) * pr_all_year_rx
           if (time == 1) {
               pr_oth_time_rx[i][time] = pr_oth_year_rx;
           }
           if (time > 1) {
               pr_oth_time_rx[i][time] = pr_oth_year_rx + pr_oth_time_rx[i][time - 1];
           }
           //* Breast mortality and recurrence as competing risk
           pr_br_year_rx = proportion_br_rx * pr_all_year_rx
           if (time == 1) {
               pr_br_time_rx[i][time] = pr_br_year_rx;
           }
           if (time > 1) {
               pr_br_time_rx[i][time] = pr_br_year_rx + pr_br_time_rx[i][time - 1];
           }
           pr_dfs_time_rx[i][time] = 1 - Math.exp(Math.log(1 - pr_br_time_rx[i][time]) * 1.3)
           // previously:
           // assign results at final time i.e. time required for results
           //if (time == ftime) {

           // Now we have 10 year results, assign results at each year
               if (i == 0) {
                   pr_oth_time_0 = pr_oth_time_rx[i][time];
                   pr_br_time_0 = pr_br_time_rx[i][time];
                   pr_all_time_0 = pr_all_time_rx[i][time];
                   pr_dfs_time_0 = pr_dfs_time_rx[i][time];
               } else {
                   //* Benefits of treatment
                   if (i == 1) {
                       //benefit_h = pr_br_time_0 - pr_br_time_rx[i][time];
                       benefit_h = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 2) {
                       //benefit_h10 = pr_br_time_0 - pr_br_time_rx[i][time];
                       benefit_h10 = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 3) {
                       //benefit_c = pr_br_time_0 - pr_br_time_rx[i][time];
                       benefit_c = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 4) {
                       //benefit_t = pr_br_time_0 - pr_br_time_rx[i][time];
                       benefit_t = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 5) {
                       //benefit_hc = pr_br_time_0 - pr_br_time_rx[i][time];
                       benefit_hc = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 6) {
                       //benefit_h10c = pr_br_time_0 - pr_br_time_rx[i][time];
                       benefit_h10c = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 7) {
                       //benefit_hct = pr_br_time_0 - pr_br_time_rx[i][time];
                       benefit_hct = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 8) {
                       //benefit_h10ct = pr_br_time_0 - pr_br_time_rx[i][time];
                       benefit_h10ct = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 9) {
                       benefit_h_low = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 10) {
                       benefit_h_high = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 11) {
                       benefit_c_low = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 12) {
                       benefit_c_high = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 13) {
                       benefit_t_low = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 14) {
                       benefit_t_high = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 15) {
                       benefit_hc_low = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 16) {
                       benefit_hc_high = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 17) {
                       benefit_hct_low = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   if (i == 18) {
                       benefit_hct_high = pr_all_time_0 - pr_all_time_rx[i][time];
                   }
                   // unused:
                   //gen benefit_rx = pr_br_time_0[time] - pr_br_time_rx[time]
                   //gen benefit_dfs_rx = pr_dfs_time_0[time] - pr_dfs_time_rx[time]
               }

            //}

            // No deaths at time = 0
            pr_oth_time_rx[i][0] = 0;
            pr_br_time_rx[i][0] = 0;

       } // end of types loop h,c etc


       results_time[time] = {
           // outputs required for predict bar chart display
           bcSpecSur: 1.0 - pr_br_time_0,
           cumOverallSurOL: 1.0 - pr_oth_time_0 - pr_br_time_0,
           cumOverallSurHormo: benefit_h,
           cumOverallSurChemo: benefit_c,
           cumOverallSurCandH: benefit_hc,
           cumOverallSurCHT: benefit_hct,
           /* reverse the low - high order here because a higher hazard corresponds to a lower survival */
           marginSurHormo: [benefit_h_high, benefit_h_low],
           marginSurChemo: [benefit_c_high, benefit_c_low],
           marginSurCandH: [benefit_hc_high, benefit_hc_low],
           marginSurCHT: [benefit_hct_high, benefit_hct_low],

           oth: (1 - surv_oth_time[time]) // was pr_oth_time_0 but (1 - surv_oth_time[time]) is more likely correct!
       }
    } // end of time loop
    return results_time;
}

function create2DArray(rows, cols) {
    var arr = [];
    for (var i = 0; i < rows; i++) {
        arr[i] = new Array(cols);
        for (var j = 0; j < cols; j++) {
            arr[i][j] = 0.0;
        }
    }
    return arr;
}
