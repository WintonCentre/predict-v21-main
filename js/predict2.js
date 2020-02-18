/* Perform survival prediction, reading values from one form, and populating a second. */
function perform_prediction() {
  var age, size, nodes, micromet;
  age = parseInt(document.getElementById("age").value);
  if (document.getElementById("size").value.match(/^ *$/)) {
    size = 9998; // Unknown for web interface
  } else {
    size = parseInt(document.getElementById("size").value);
  }
  if (document.getElementById("nodes").value.match(/^ *$/)) {
    nodes = 9998; // Unknown for web interface
  } else {
    nodes = parseInt(document.getElementById("nodes").value);
  }
  micromet = 0; // 0 unless box checked on web interface
  if ( document.getElementById("micromet").checked == true ) {micromet = 1;}
  var form = document.forms["predictForm"];

  var grade = parseInt(get_radio_value(form.grade));
  var erstat = parseFloat(get_radio_value(form.erstat));
  var detection = parseFloat(get_radio_value(form.detection));
  var comor = 0; //Has no effect on model. Was: parseInt(get_radio_value(form.comor));
  var chemoGen = parseInt(get_radio_value(form.chemoGen));
  var her2 = parseFloat(get_radio_value(form.her2));
  var ki67 = parseFloat(get_radio_value(form.ki67));
  if (document.getElementById("age").value == "" ||
      grade == -9999 || erstat == -9999 || detection == -9999 ||
      comor == -9999 || chemoGen == -9999 ||
      her2 == -9999 || ki67 == -9999) {
    alert("Please complete all fields before predicting survival.");
    return; // Abort if any fields blank / radio boxes unchecked
  }
  if (age < 0 || size < 0 || nodes < 0) {
    alert("Negative age / tumour size / number of nodes is not possible.");
    return; // Abort if invalid arguments
  }
  if ( micromet == 1 ) { nodes=0.5; }     // reset nodes if micromet checkbox is checked
// n.b. size & nodes fields are set to 9998 above if left blank in web form if unknown
  if ( size == 9998 || nodes == 9998) {
    alert("Undefined tumour size / number of nodes is not allowable with V2.0.");
    return; // Abort if invalid arguments
  }

  try {
    var pageTracker = _gat._getTracker("UA-1462795-8");
    pageTracker._trackPageview('predict_v2.0');
  } catch(err) {}
  var result = predict_v2_0(age, size, nodes, grade, erstat, detection, chemoGen, her2, ki67, 5.0);

  var bcSpecSur = result[0];
  var cumOverallSurOL = result[1];
  var cumOverallSurHormo = result[2];
  var cumOverallSurChemo = result[3];
  var cumOverallSurCandH = result[4];
  var cumOverallSurCHT = result[5]
  var pySurv10OL = result[6];
  var pySurv10Chemo = result[7];
  var pySurv10Hormo = result[8];
  var pySurv10CandH = result[9];
  var pySurv10CHT = result[10];

  // n.b in V2.0 onwards treatment vars are no longer cumulative overall based so dont subtract ovs

  ovs = cumOverallSurOL;
  document.getElementById("surv5_bcSpec").value = percent(bcSpecSur);
  document.getElementById("surv5_OL").value = percent(ovs);
  document.getElementById("surv5_Chemo").value = percent(cumOverallSurChemo);
  document.getElementById("surv5_Hormo").value = percent(cumOverallSurHormo);
  document.getElementById("surv5_CandH").value = percent(cumOverallSurCandH);
  document.getElementById("surv5_CHT").value = percent(cumOverallSurCHT);
  var chart5_ovs = chart_percent(ovs);
  var chart5_hormo = chart_percent(cumOverallSurHormo);
  var chart5_chemo = chart_percent(cumOverallSurChemo);
  var chart5_candh = chart_percent(cumOverallSurCandH);
  var chart5_cht = chart_percent(cumOverallSurCHT)
  // "Fix" rounding of the chart, so that it shows the difference between rounded values, rather than the rounded difference between values
  var chart5_chemo_add = chart_percent(parseFloat(chart5_candh)/100.0 - parseFloat(chart5_hormo)/100.0);
  // Still correct for ER negative, as hormone benefit is then 0
  var chart5_tram_add = chart_percent(parseFloat(chart5_cht)/100.0 - parseFloat(chart5_candh)/100.0);
  var text5_ovs = text_percent(ovs);
  var text5_hormo = text_percent(cumOverallSurHormo);
  var text5_chemo = text_percent(cumOverallSurChemo);
  var text5_candh = text_percent(cumOverallSurCandH);
  var text5_cht = text_percent(cumOverallSurCHT)

  result = predict_v2_0(age, size, nodes, grade, erstat, detection, chemoGen, her2, ki67, 10.0);

  var bcSpecSur = result[0];
  var cumOverallSurOL = result[1];
  var cumOverallSurHormo = result[2];
  var cumOverallSurChemo = result[3];
  var cumOverallSurCandH = result[4];
  var cumOverallSurCHT = result[5]
  var pySurv10OL = result[6];
  var pySurv10Chemo = result[7];
  var pySurv10Hormo = result[8];
  var pySurv10CandH = result[9];
  var pySurv10CHT = result[10];
  ovs = cumOverallSurOL;
  document.getElementById("surv10_bcSpec").value = percent(bcSpecSur);
  document.getElementById("surv10_OL").value = percent(ovs);
  document.getElementById("surv10_Chemo").value = percent(cumOverallSurChemo);
  document.getElementById("surv10_Hormo").value = percent(cumOverallSurHormo);
  document.getElementById("surv10_CandH").value = percent(cumOverallSurCandH);
  document.getElementById("surv10_CHT").value = percent(cumOverallSurCHT);
  var chart10_ovs = chart_percent(ovs);
  var chart10_hormo = chart_percent(cumOverallSurHormo);
  var chart10_chemo = chart_percent(cumOverallSurChemo);
  var chart10_candh = chart_percent(cumOverallSurCandH);
  var chart10_cht = chart_percent(cumOverallSurCHT)
  // "Fix" rounding of the chart, so that it shows the difference between rounded values, rather than the rounded difference between values
  var chart10_chemo_add = chart_percent(parseFloat(chart10_candh)/100.0 - parseFloat(chart10_hormo)/100.0);
  // Still correct for ER negative, as hormone benefit is then 0
  var chart10_tram_add = chart_percent(parseFloat(chart10_cht)/100.0 - parseFloat(chart10_candh)/100.0);
  var text10_ovs = text_percent(ovs);
  var text10_hormo = text_percent(cumOverallSurHormo);
  var text10_chemo = text_percent(cumOverallSurChemo);
  var text10_candh = text_percent(cumOverallSurCandH);
  var text10_cht = text_percent(cumOverallSurCHT)

  ovs = pySurv10OL;
  document.getElementById("py_OL").value = (ovs).toFixed(2);
  document.getElementById("py_Chemo").value = (pySurv10Chemo-ovs).toFixed(2);
  document.getElementById("py_Hormo").value = (pySurv10Hormo-ovs).toFixed(2);
  document.getElementById("py_CandH").value = (pySurv10CandH-ovs).toFixed(2);
  document.getElementById("py_CHT").value = (pySurv10CHT-ovs).toFixed(2);

// make sure graph variables that should be zero are zero
// this can occur since these are all calculated regardless of regime
  if (erstat == 1) {
    // ER positive  (have hormone therapy)
    if (chemoGen == 0) {
      chart5_chemo_add=0.0;
      chart10_chemo_add=0.0;
    }
  } else {
    // ER negative  (no hormone therapy)
    chart5_hormo=0.0;
    chart10_hormo=0.0;
    if (chemoGen == 0) {
      chart5_chemo_add=0.0;
      chart10_chemo_add=0.0;
    }
  }
  // Can only have tras effect if HER2 positive and a chemo regime
  if (her2 != 1 || chemoGen == 0) {
    chart5_tram_add=0.0;
    chart10_tram_add=0.0;
  }
  document.getElementById('chart5_ovs').value = chart5_ovs;
  document.getElementById('chart5_hormo').value = chart5_hormo;
  document.getElementById('chart5_chemo_add').value = chart5_chemo_add;
  document.getElementById('chart5_tram_add').value = chart5_tram_add;
  document.getElementById('chart10_ovs').value = chart10_ovs;
  document.getElementById('chart10_hormo').value = chart10_hormo;
  document.getElementById('chart10_chemo_add').value = chart10_chemo_add;
  document.getElementById('chart10_tram_add').value = chart10_tram_add;

  // Update textual summary
  document.getElementById("surv5_OL_100").innerHTML= text5_ovs;
  document.getElementById("surv5_Hormo_100").innerHTML= text5_hormo;
  document.getElementById("surv5_Chemo_100").innerHTML= text5_chemo;
  document.getElementById("surv5_CandH_100").innerHTML= text5_candh;
  document.getElementById("surv5_CHT_100").innerHTML= text5_cht;
  document.getElementById("surv10_OL_100").innerHTML= text10_ovs;
  document.getElementById("surv10_Hormo_100").innerHTML= text10_hormo;
  document.getElementById("surv10_Chemo_100").innerHTML= text10_chemo;
  document.getElementById("surv10_CandH_100").innerHTML= text10_candh;
  document.getElementById("surv10_CHT_100").innerHTML= text10_cht;

  // Hide or show appropriate textual fields
  var er_pos_display, er_neg_display, cht_extra_text, display;
  if (erstat == 1) {
     // ER positive
     er_pos_display = "inline";
     er_neg_display = "none";
     cht_extra_text = 'hormone therapy, '
  } else {
     er_pos_display = "none";
     er_neg_display = "inline";
     cht_extra_text = ''
  }
  for (var i = 1; i <= 4; i++) {
    if (chemoGen == 0 && (i % 2 == 0)) {
      display = "none";
    } else {
      display = er_pos_display;
    }
    document.getElementById("er_pos_only_" + i).style.display = display;
  }
  for (var i = 1; i <= 2; i++) {
    if (chemoGen == 0) {
      display = "none";
    } else {
      display = er_neg_display;
    }
    document.getElementById("er_neg_only_" + i).style.display = display;
  }
  for (var i = 1; i <= 2; i++) {
    if (her2 == 1 && chemoGen > 0) {
      display = "inline";
    } else {
      display = "none";
    }
    document.getElementById("her2_pos_only_" + i).style.display = display;
    document.getElementById("cht_extra_text_" + i).innerHTML= cht_extra_text;
  }
  drawChart();
}
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
function predict_v2_0(age, size, nodes, grade, erstat, detection, chemoGen, her2, ki67, rtime) {

// first decode input settings for grade, detection
var detection_values=[0.0,1.0,0.204];  // inputs are (0,1,2)
detection=detection_values[detection];
var grade_values=[1.0,2.0,3.0,2.13];  // inputs are (1,2,3,9)
if ( grade == 9 ) grade=4;
grade=grade_values[grade-1];
// prevent high hazard for young patients - age of 24.54 caps Hazard ratio at 4.008
if ( age < 25 ) { age=25 };
var grade_a=0;
if( grade == 2 || grade == 3 ) { grade_a=1; }
//console.log("check grade,detection: ",grade,detection);
/* n.b. default of 0 for her2_rh and ki67_rh remains when inputs are set as undefined */
var her2_rh = 0;
if ( her2 == 1 && erstat == 1 ) { her2_rh = 0.2413; }
if ( her2 == 0 && erstat == 1 ) { her2_rh = -0.0762; }
if ( her2 == 1 && erstat == 0 ) { her2_rh = 0.2413; }
if ( her2 == 0 && erstat == 0 ) { her2_rh = -0.0762; }
var ki67_rh = 0;
if ( ki67 == 1 && erstat == 1 ) { ki67_rh = 0.14904; }
if ( ki67 == 0 && erstat == 1 ) { ki67_rh = -0.11333; }

var c = 0;
// No chemo - use initial value of c i.e. 0
// First chemoGen - not used here for completeness
if ( chemoGen == 1 ) {
  if ( age<50 && erstat == 0 ) { c = -0.3567; }
  if ( age>=50 && age<60 && erstat == 0 ) { c = -0.2485; }
  if ( age>=60 && erstat == 0 ) { c = -0.1278; }
  if ( age<50 && erstat == 1 ) { c = -0.3567; }
  if ( age>=50 && age<60 && erstat == 1 ) { c = -0.1744; }
  if ( age>=60 && erstat == 1 ) { c = -0.0834; }
}
// Second chemoGen
if ( chemoGen == 2 ) {
  c=-0.248;
}
// Third chemoGen
if ( chemoGen == 3 ) {
  c=-0.446;
}

var surv_oth_time=[];
var surv_oth_year=[];
var bs=[];
surv_oth_time[0]=0;
surv_oth_year[0]=0;
bs[0]=0;
// The number of columns is not important, as it is not required to specify the size of an array before using it
var ftime=Math.round(rtime);
var mort_rate_cum_rx=create2DArray(9,ftime+1);
var surv_br_time_rx=create2DArray(9,ftime+1);
var pr_all_time_rx=create2DArray(9,ftime+1);
var pr_oth_time_rx=create2DArray(9,ftime+1);
var pr_br_time_rx=create2DArray(9,ftime+1);
var pr_dfs_time_rx=create2DArray(9,ftime+1);
var benefit_h,benefit_h10,benefit_c,benefit_t,benefit_hc,benefit_h10c,benefit_hct,benefit_h10ct;
var time;
for ( time=1 ; time <= ftime; ++time) {

var pi;
// Calculate the breast cancer mortality prognostic index (pi)
// Generate baseline survival
if ( erstat == 1 ) {
  pi = 34.53642 * (Math.pow(age/10.0,-2.0) - 0.0287449295)
		 - 34.20342 * (Math.pow(age/10.0,-2.0) * Math.log(age/10.0) - 0.0510121013)
		 + 0.7530729 * (Math.log(size/100.0) + 1.545233938)
		 + 0.7060723 * (Math.log((nodes+1)/10.0) + 1.387566896)
                 + 0.746655 * grade
	         - 0.22763366 * detection
		 + her2_rh + ki67_rh;
  bs[time] = Math.exp(0.7424402 - 7.527762*(Math.pow(1.0/time,0.5)) - 1.812513*Math.pow(1.0/time,0.5)*Math.log(time));
}
if ( erstat == 0 ) {
  pi = 0.0089827 * (age - 56.3254902)
       + 2.093446 * (Math.pow(size/100.0,0.5) - 0.5090456276)
       + 0.6260541 * (Math.log((nodes+1)/10.0) + 1.086916249)
       + 1.129091 * grade_a
       + her2_rh + ki67_rh;
  bs[time] = Math.exp(-1.156036 + 0.4707332/Math.pow(time,2.0) - 3.51355/time);
}

// Generate therapy reduction coefficients
var h = 0;
var t = 0;
if ( erstat == 1 ) { h = -0.3857; }
var h10 = h;
if ( erstat == 1 && time > 10 ) { h10 = h - 0.3425; }    // Relative risk 0.71 years 11-15

if ( her2 == 1 ) { t = -0.3567; }
//console.log("(her2_rh,ki67_rh,pi,bs,h,c,t):",her2_rh,ki67_rh,pi,bs,h,c,t);

var hc = h + c;
var hct =  h + c + t;
var h10c = h10 + c;
var h10ct = h10 + c + t;

var types=[0,h,h10,c,t,hc,h10c,hct,h10ct];

// Generate cumulative survival non-breast mortality

var bs_oth = Math.exp(-6.052919 + 1.079863*Math.log(time) + 0.3255321*Math.pow(time,0.5));
surv_oth_time[time] = Math.exp(-Math.exp(0.0698252*(Math.pow(age/10.0,2.0)-34.23391957))*bs_oth);

// Generate annual survival from cumulative survival
if ( time == 1 ) { surv_oth_year[time] = 1 - surv_oth_time[time];}
if ( time > 1 ) { surv_oth_year[time] = surv_oth_time[time-1] - surv_oth_time[time];}

var mort_rate_rx,surv_br_year_rx,pr_all_year_rx,proportion_br_rx,pr_oth_year_rx,pr_br_year_rx;
var pr_oth_time_0;
var pr_br_time_0;
var pr_dfs_time_0;

for (var i = 0; i < types.length; i++) {
var rx=types[i];

//* Generate the breast cancer specific survival

if ( time == 1 ) { mort_rate_rx = bs[time]*Math.exp(pi + rx); }
if ( time > 1 ) { mort_rate_rx = (bs[time] - bs[time-1])*Math.exp(pi + rx); }
if ( time == 1 ) { mort_rate_cum_rx[i][time] = mort_rate_rx; }
if ( time > 1 ) { mort_rate_cum_rx[i][time] = mort_rate_rx + mort_rate_cum_rx[i][time-1]; }
surv_br_time_rx[i][time] = Math.exp(- mort_rate_cum_rx[i][time])
if ( time == 1 ) { surv_br_year_rx = 1 - surv_br_time_rx[i][time]; }
if ( time > 1 ) { surv_br_year_rx = surv_br_time_rx[i][time-1] - surv_br_time_rx[i][time]; }

//* All cause mortality

pr_all_time_rx[i][time] = 1 - surv_oth_time[time]*surv_br_time_rx[i][time];     // Cumulative all cause mortality
if ( time == 1 ) { pr_all_year_rx = pr_all_time_rx[i][time]; }            // Number deaths in year 1
if ( time > 1 ) { pr_all_year_rx = pr_all_time_rx[i][time] - pr_all_time_rx[i][time-1]; }

//* Proportion of all cause mortality

proportion_br_rx = (surv_br_year_rx)/(surv_oth_year[time] + surv_br_year_rx)
pr_oth_year_rx = (1 -  proportion_br_rx)*pr_all_year_rx
if ( time == 1 ) { pr_oth_time_rx[i][time] = pr_oth_year_rx; }
if ( time > 1 ) { pr_oth_time_rx[i][time] = pr_oth_year_rx + pr_oth_time_rx[i][time-1]; }

//* Breast mortality and recurrence as competing risk

pr_br_year_rx = proportion_br_rx*pr_all_year_rx
if ( time == 1 ) { pr_br_time_rx[i][time] = pr_br_year_rx; }
if ( time > 1 ) { pr_br_time_rx[i][time] = pr_br_year_rx + pr_br_time_rx[i][time-1]; }
pr_dfs_time_rx[i][time] = 1-Math.exp(Math.log(1-pr_br_time_rx[i][time])*1.3)

// assign results at final time i.e. time required for results
if ( time == ftime ) {
if ( i == 0 ) {
  pr_oth_time_0=pr_oth_time_rx[i][time];
  pr_br_time_0=pr_br_time_rx[i][time];
  pr_dfs_time_0=pr_dfs_time_rx[i][time];
} else {
//* Benefits of treatment
  if ( i == 1 ) { benefit_h=pr_br_time_0 - pr_br_time_rx[i][time]; }
  if ( i == 2 ) { benefit_h10=pr_br_time_0 - pr_br_time_rx[i][time]; }
  if ( i == 3 ) { benefit_c=pr_br_time_0 - pr_br_time_rx[i][time]; }
  if ( i == 4 ) { benefit_t=pr_br_time_0 - pr_br_time_rx[i][time]; }
  if ( i == 5 ) { benefit_hc=pr_br_time_0 - pr_br_time_rx[i][time]; }
  if ( i == 6 ) { benefit_h10c=pr_br_time_0 - pr_br_time_rx[i][time]; }
  if ( i == 7 ) { benefit_hct=pr_br_time_0 - pr_br_time_rx[i][time]; }
  if ( i == 8 ) { benefit_h10ct=pr_br_time_0 - pr_br_time_rx[i][time]; }
//gen benefit_rx = pr_br_time_0[time] - pr_br_time_rx[time]
//gen benefit_dfs_rx = pr_dfs_time_0[time] - pr_dfs_time_rx[time]
}
}

}    // end of types loop h,c etc

}    // end of time loop

// Additive Benefits - these are hierarchical

var bcSpecSur=1.0 - pr_br_time_0;
// outputs required for predict bar chart display
var cumOverallSurOL=1.0 - pr_oth_time_0 - pr_br_time_0;
var cumOverallSurHormo=benefit_h;
var cumOverallSurChemo=benefit_c;
var cumOverallSurCandH=benefit_hc;
var cumOverallSurCHT=benefit_hct;
/*
  n.b. this is original predict (V1) return line
  return [bcSpecSur, cumOverallSurOL, cumOverallSurChemo, cumOverallSurHormo, cumOverallSurCandH, cumOverallSurCHT,
          pySurv10OL, pySurv10Chemo, pySurv10Hormo, pySurv10CandH, pySurv10CHT];
  n.b. this bcSpecSur is not displayed
*/
  return [bcSpecSur, cumOverallSurOL, cumOverallSurHormo, cumOverallSurChemo, cumOverallSurCandH, cumOverallSurCHT,
          0.0, 0.0, 0.0, 0.0, 0.0];
}
function create2DArray(rows,cols) {
  var arr = [];
  for (var i=0;i<rows;i++) {
    arr[i] = new Array(cols);
    for(var j=0; j < cols; j++){
      arr[i][j] = 0.0;
    }
  }
  return arr;
}
/* Returns the contents of a radio field, or "-9999" if nothing selected. */
function get_radio_value(field) {
  var result = "-9999";
  for (var i=0; i < field.length; i++) {
    if (field[i].checked) {
      result = field[i].value;
    }
  }
  return result;
}

/* Format a floating point number as a percentage */
function percent(x) {
  return "" + (x*100).toFixed(1) + "%";
}

/* Format a floating point number as a percentage value, for a chart */
function chart_percent(x) {
  return "" + (x*100).toFixed(1);
}

/* Format a floating point number as a percentage value, for a textual description */
function text_percent(x) {
  return "" + (x*100).toFixed(0);
}
/* Reset chart images and text display */
function reset_displays() {
  document.getElementById('chart5_ovs').value = "";
  document.getElementById('chart5_hormo').value = "";
  document.getElementById('chart5_chemo_add').value = "";
  document.getElementById('chart5_tram_add').value = "";
  document.getElementById('chart10_ovs').value = "";
  document.getElementById('chart10_hormo').value = "";
  document.getElementById('chart10_chemo_add').value = "";
  document.getElementById('chart10_tram_add').value = "";
  resetChart();
// reset text under graph legend
  document.getElementById("surv5_OL_100").innerHTML= "XX";
  document.getElementById("surv5_OL_100").style.display = "display";
  document.getElementById("surv10_OL_100").innerHTML= "XX";
  document.getElementById("surv10_OL_100").style.display = "display";
  for (var i = 1; i <= 4; i++) {
    document.getElementById("er_pos_only_" + i).style.display = "none";
  }
  for (var i = 1; i <= 2; i++) {
    document.getElementById("er_neg_only_" + i).style.display = "none";
  }
  for (var i = 1; i <= 2; i++) {
    document.getElementById("her2_pos_only_" + i).style.display = "none";
  }
}
