//<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>
//<script type="text/javascript">
  google.charts.load('42', {packages: ['corechart']});
  google.charts.setOnLoadCallback(resetChart);     // on first load display the null chart
  function drawChart() {

// n.b. need all these retrieved;
    var chart5_ovs = parseFloat(document.getElementById('chart5_ovs').value);
    var chart5_hormo = parseFloat(document.getElementById('chart5_hormo').value);
    var chart5_chemo_add = parseFloat(document.getElementById('chart5_chemo_add').value);
    var chart5_tram_add = parseFloat(document.getElementById('chart5_tram_add').value);
    var chart10_ovs = parseFloat(document.getElementById('chart10_ovs').value);
    var chart10_hormo = parseFloat(document.getElementById('chart10_hormo').value);
    var chart10_chemo_add = parseFloat(document.getElementById('chart10_chemo_add').value);
    var chart10_tram_add = parseFloat(document.getElementById('chart10_tram_add').value);
    var types=['Year','Survival with no Adjuvant treatment',
               'Benefit of Adjuvant Hormone therapy'];
    if ( chart5_chemo_add > 0.0001 ) {
      types[types.length]='Additional benefit of Adjuvant Chemotherapy';
    }
    if ( chart5_tram_add > 0.0001 ) {
      types[types.length]='Additional benefit of Trastuzumab';
    }
    types[types.length]= { role: 'annotation' };
    var yr5 =  ['Five years', chart5_ovs, chart5_hormo];
    var yr10 = ['Ten years', chart10_ovs, chart10_hormo];
    if ( chart5_chemo_add > 0.0001 ) {
      yr5[yr5.length]= chart5_chemo_add;
      yr10[yr10.length]= chart10_chemo_add;
    }
    if ( chart5_tram_add > 0.0001 ) {
      yr5[yr5.length]= chart5_tram_add;
      yr10[yr10.length]= chart10_tram_add;
    }
    yr5[yr5.length]= '';
    yr10[yr10.length]= '';
    var data = google.visualization.arrayToDataTable([types,yr5,yr10]);

// alternate construction - cant get this to work
//  var data = google.visualization.DataTable();
//  data.addColumn({ type: 'number', label: 'Five years', id: 'year5', role: 'annotation' });
//  data.addColumn({ type: 'number', label: 'Ten years', id: 'year10', role: 'annotation' });
//  data.addRow([10, 24]);
//  data.addRow([16, 22]);

    var view = new google.visualization.DataView(data);
// n.b. view.setColumns() Specifies which columns are visible in this view. Any columns not specified will be hidden
    var setcols=[0,1,{ calc: "stringify",sourceColumn: 1,type: "string",role: "annotation" }];
// only set the hormo benefit value (column 2) to be displayed if it's > 0.0
// n.b. hormo value is always in the data array so always index 2
    if ( chart5_hormo > 0.0001 ) {
      setcols[setcols.length]= 2;
      setcols[setcols.length]= { calc: "stringify",sourceColumn: 2,type: "string",role: "annotation" };
    } else {
//    setcols[setcols.length]= 2;     # not needed as not displaying ??
    }
// n.b. if present chemo is always index 3 and traz always index 4 (no traz without chemo)
    if ( chart5_chemo_add > 0.0001 ) {
      setcols[setcols.length]= 3;
      setcols[setcols.length]= { calc: "stringify",sourceColumn: 3,type: "string",role: "annotation" };
    } else {
//    setcols[setcols.length]= 3;
    }
    if ( chart5_tram_add > 0.0001 ) {
      setcols[setcols.length]= 4;
      setcols[setcols.length]= { calc: "stringify",sourceColumn: 4,type: "string",role: "annotation" };
//    setcols[setcols.length]= 5;
    }
    setcols[setcols.length]= types.length - 1;
    view.setColumns(setcols);
//  view.setColumns([0,     // this example for labelling of 2 data regimes in bar chart
//                   1,{ calc: "stringify",sourceColumn: 1,type: "string",role: "annotation" },
//                   2,{ calc: "stringify",sourceColumn: 2,type: "string",role: "annotation" },3]);

// n.b. height & width are set as same as in img block
//    legend:{ alignment: 'bottom', textStyle: { fontSize: getFontSize(), overflow: 'auto'} }
//    legend: {position: 'bottom', alignment: 'center', maxLines: 4, textStyle: { overflow: 'none'}},
//  legend is uncontrolable - especially for long labels like we have! so don't use and replace with static png
// n.b. give chart max y-axis of 105 so top category value label has enough room to display
//    tooltips: {isHtml: true, showColorCode: true},
//    colors: ['#666699','#00ff00','#ff9900','#00aa99'],
// n.b. since new release of google charts on 23/2/15 for ER -ve (0 hormo) we now need to remove the green
// from the colours vector as it's being used for the chemo i.e. zero hormo is not being interpretted
// properly seemingly a bug - corrected 17/3/15 by emd
    var cols = ['#666699'];
    if ( chart5_hormo > 0.001 ) {
      cols[cols.length]= '#00ff00';
    }
    cols[cols.length]= '#ff9900';
    cols[cols.length]= '#00aa99';
    var options = {
      title: 'Overall Survival at 5 and 10 years (percent)',
      colors: cols,
      vAxis: {maxValue: 105, ticks: [0,10,20,30,40,50,60,70,80,90,100]},
      chartArea: {left:40, top:30, width:'70%', height:'65%'},
      legend: {position: 'none'},
      bar: {groupWidth: "50%"},
      isStacked: true,
      width: 360,
      height: 460
    };

    var chart_div = document.getElementById('chart1');
    var chart = new google.visualization.ColumnChart(chart_div);

// n.b. currently turned off as causes tooltips NOT to be displayed on mouseover -must have tooltips!
    // Wait for the chart to finish drawing before calling the getImageURI() method.
//  google.visualization.events.addListener(chart, 'ready', function () {
//    chart_div.innerHTML = '<img src="' + chart.getImageURI() + '">';
//    console.log(chart_div.innerHTML);
//  });

//  chart.draw(data, options);     // use this form if not using the view object for labelling
    chart.draw(view, options);

  }
  function resetChart() {
    var types=['Year','Survival with no Adjuvant treatment',
               'Benefit of Adjuvant Hormone therapy', { role: 'annotation' }];
    var yr5 =  ['Five years', 0.1, 0.1, ''];
    var yr10 = ['Ten years', 0.1, 0.1, ''];
    var data = google.visualization.arrayToDataTable([types,yr5,yr10]);
    var options = {
      title: 'Overall Survival at 5 and 10 years (percent)',
      colors: ['#666699','#00ff00','#ff9900','#00aa99'],
      vAxis: {maxValue: 105, ticks: [0,10,20,30,40,50,60,70,80,90,100]},
      chartArea: {left:40, top:30, width:'70%', height:'65%'},
      legend: {position: 'none'},
      bar: {groupWidth: "50%"},
      isStacked: true,
      width: 360,
      height: 460
    };
//    tooltips: {isHtml: true},

    var chart_div = document.getElementById('chart1');
    var chart = new google.visualization.ColumnChart(chart_div);

// Wait for the chart to finish drawing before calling the getImageURI() method.
//  google.visualization.events.addListener(chart, 'ready', function () {
//    chart_div.innerHTML = '<img src="' + chart.getImageURI() + '">';
//    console.log(chart_div.innerHTML);
//  });

    chart.draw(data, options);     // use this form if not using the view object for labelling

  }
