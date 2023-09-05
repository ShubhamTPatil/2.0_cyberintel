<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Nandakumar Sankaralingam -->
--%>
<%@ page import="com.marimba.apps.subscription.common.ISubscriptionConstants" contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps" %>

<!DOCTYPE html>
<html lang="en">
<head>

<title>DefenSight</title>

<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap-icons.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/all.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/datatables.min.css"/>
<link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/style.css"/>

<script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/datatables.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/common.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/d3.v6.min.js"></script>


<script type="text/javascript">

$(function () {

    $('#anomalyReport').addClass('nav-selected');

    var data = [];

    /*
    $.ajax({
        url: './anomoly.do',
        type: 'POST',
        dataType: 'text json',
        data: {action: 'heatmapData'},
        beforeSend: function() {},
        complete: function (xhr, status) {},
        success: function (response) {
            console.log("succuess");
            console.log(JSON.stringify(response));
            loadHeatMap(data);
        }
    });
    */

    loadHeatMap(data);

    // Sample data (replace this with your actual data)
    var chart1Data = {
        labels: ["Type 1", "Type 2", "Type 3", "Type 4"],
        datasets: [{
            data: [30, 40, 15, 15],
            backgroundColor: ['#4A87B5', '#49A5DE', '#71DCEB', '#7D8EDF'],
        }]
    };

    var chart2Data = {
        labels: ["Type 1", "Type 2", "Type 3", "Type 4"],
        datasets: [{
            data: [15, 30, 40, 15],
            backgroundColor: ['#4A87B5', '#49A5DE', '#71DCEB', '#7D8EDF'],
        }]
    };

    // Configure the chart options
    var chartOptions = {
        responsive: false,
        maintainAspectRatio: false,
        cutoutPercentage: 0,
        elements: {
            arc: {
                borderWidth: 0,
            }
        },
        plugins: {
            legend: {
                display: true,
                position: 'bottom',
                labels: {
                  boxWidth: 20, // Set the desired width of the legend
                }
            }
        }
    };

    // Create the first pie chart
    var ctx1 = document.getElementById('chart1').getContext('2d');
    var chart1 = new Chart(ctx1, {
        type: 'pie',
        data: chart1Data,
        options: chartOptions
    });

    // Create the second pie chart
    var ctx2 = document.getElementById('chart2').getContext('2d');
    var chart2 = new Chart(ctx2, {
        type: 'pie',
        data: chart2Data,
        options: chartOptions
    });

});

function loadHeatMap(data) {

    let numCols = 0;
    const startTime = new Date().getTime();

    if(data.length == 0) {
        // Sample data
        const numRows = 20;
        numCols = 60;
        for (let i = 0; i < numRows; i++) {
            const rowData = [];
            for (let j = 0; j < numCols; j++) {
                const time = new Date(startTime + j * 7 * 24 * 60 * 60 * 1000);
                rowData.push({
                    state: Math.random() < 0.1 ? 'Anomalous' : (Math.random() < 0.5 ? 'Active' : 'Inactive'),
                    time: time,
                    machine: `Machine ${i + 1}`
                });
            }
            data.push(rowData);
        }
    } else {
        numCols = data[0].length;
    }

     console.log(data.length)

     console.log(data)

    // Calculate width and height    
    const h = data.length * 30;
    const w = document.getElementById("my_dataviz").clientWidth;

    // Set dimensions and margins
    const margin = { top: 30, right: 40, bottom: 60, left: 140 };
    const width = w - margin.left - margin.right;
    const height = h - margin.top - margin.bottom;
     
    // Append SVG element
    // const svg = d3.select("#my_dataviz")
    //     .append("svg")
    //     .attr("width", width)
    //     .attr("height", height)
    //     .append("g")
    //     .attr("transform", `translate(${margin.left},${margin.top})`);

    var svg = d3.select("#my_dataviz")
        .append("svg")
        .attr("width", width + margin.left + margin.right)
        .attr("height", height + margin.top + margin.bottom)
        .append("g")
        .attr("transform",
                "translate(" + margin.left + "," + margin.top + ")");

     // Create scales
     const x = d3.scaleTime()
         .range([0, width])
         .domain([new Date(startTime), new Date(startTime + numCols * 7 * 24 * 60 * 60 * 1000)]);

     const y = d3.scaleBand()
         .range([height, 0])
         .domain(data.map((_, i) => `Machine ${i + 1}`))
         .padding(0.01);

     // Calculate cell width based on time interval
     const cellWidth = width / numCols;

     // Render the heatmap
     const cells = svg.selectAll("rect")
         .data(data.flat())
         .enter().append("rect")
         .attr("x", (d, i) => i % numCols * cellWidth)
         .attr("y", (d) => y(d.machine))
         .attr("width", cellWidth)
         .attr("height", y.bandwidth())
         .attr("class", d => `cell-${d.state}`) // Add class based on state
         .attr("stroke", "white") // Add white stroke for cell borders
         .style("stroke-width", 1) // Set border width
         .on('mouseover', function (event, d) {
             const tooltip = document.getElementById("tooltip");
             tooltip.innerHTML = `Machine: ${d.machine}<br>Time: ${d.time.toLocaleString()}<br>State: ${d.state}`;

             tooltip.style.display = "block";
             tooltip.style.left = (event.pageX + 10) + "px";
             tooltip.style.top = (event.pageY + 10) + "px";
         })
         .on('mouseout', function () {
             document.getElementById("tooltip").style.display = "none";
         });

     // Add tooltips
     const tooltip = d3.select("body")
         .append("div")
         .attr("id", "tooltip")
         .style("position", "absolute")
         .style("z-index", "10")
         .style("background-color", "gray")
         .style("padding", "10px")
         .style("border", "1px solid #ccc")
         .style("border-radius", "5px")
         .style("display", "none");

     // Add x-axis (time series)
     svg.append("g")
         .attr("transform", `translate(0,${height})`)
         .call(d3.axisBottom(x));

     // Add y-axis (machine names)
     svg.append("g")
         .call(d3.axisLeft(y));

     // Add x-axis label
     svg.append("text")
         .attr("class", "axis-label")
         .attr("x", width / 2)
         .attr("y", height + margin.bottom - 10)
         .style("text-anchor", "middle")
         .text("Time");

     // Add y-axis label
     svg.append("text")
         .attr("class", "axis-label")
         .attr("transform", "rotate(-90)")
         .attr("x", -height / 2)
         .attr("y", -margin.left + 20)
         .style("text-anchor", "middle")
         .text("Machine Names");

}

</script>

<style>

    .form-select {
        width: fit-content; 
    }

    /* Define styles for different cell states */
    .cell-Anomalous {
        fill: maroon;
    }

    .cell-Active {
        fill: gray;
    }

    .cell-Inactive {
        fill: silver;
    }

    .d3-tip {
        font-size: 14px;
        line-height: 1.5;
        padding: 10px;
        background-color: gray;
        border: 1px solid #ccc;
        border-radius: 5px;
        box-shadow: 2px 2px 5px rgba(0, 0, 0, 0.1);
    }

    /* Define styles for legend */
    .legend {
        display: flex;
        justify-content: space-around;
        margin-top: -5px;
        margin-right: 500px;
    }

    .legend-item {
        display: inline-flex;
        align-items: center;
        margin-right: 5px;
    }

    .legend-color {
        width: 12px;
        height: 12px;
        margin-right: 5px;
    }

</style>


</head>
<body>

    <jsp:include page="header.jsp" />
	<jsp:include page="sidebar.jsp" />

	<main id="main" class="main">
	<div class="pagetitle">

        <div class="d-flex bd-highlight justify-content-center">
				<div class="p-2 flex-grow-1 bd-highlight">
					<span class="pagename">Anomaly Report</span> <span
						data-bs-toggle="tooltip" data-bs-placement="right"
						title="Anomaly Report"><i
						class="fa-solid fa-circle-info text-primary"></i></span>
				</div>
				<div class="refresh p-2 bd-highlight text-primary align-self-center"
					data-bs-toggle="tooltip" data-bs-placement="right" title="Refresh"
					style="cursor: pointer;">
					<i class="fa-solid fa-arrows-rotate"></i>
				</div>
				<div class="p-2 bd-highlight text-primary align-self-center">
					<a href="/shell/dashboard.do"><i
						class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>
						CMS Home </a>
				</div>
			</div>
		</div>
		

		
		<section class="section dashboard">

            <div class="row">

                <div class="card info-card">
                    <div class="card-body" id="heatMapContainer">
                        <h5 class="card-title" style="margin: 0;">Top Level Statistics</h5>
                        <div style="display: inline-flex;">
                
                            <select class="form-select form-select-sm">
                                <option value="optionA">APPLICATION</option>
                                <option value="optionA">All</option>
                                <option value="option1">Application 1</option>
                                <option value="option2">Application 2</option>
                                <option value="option3">Application 3</option>
                            </select>

                            <select class="form-select form-select-sm">
                                <option value="option1">SUBNETS</option>
                                <option value="optionA">127.0.0.1</option>
                                <option value="optionB">127.0.0.2</option>
                                <option value="optionC">255.255.0.1</option>
                            </select>

                            <select class="form-select form-select-sm">
                                <option value="choice1">OS</option>
                                <option value="choiceX">Windows</option>
                                <option value="choiceY">Linux</option>
                            </select>

                            <select class="form-select form-select-sm">
                                <option value="item1">STATUS</option>
                                <option value="item2">Anomalous</option>
                                <option value="item3">Active</option>
                                <option value="item3">Inactive</option>
                            </select>
                        </div>
                        <div style="float: right;">
                            <select class="form-select form-select-sm">
                                <option>PAST 1 Min</option>
                                <option>PAST 5 Mins</option>
                                <option>PAST 10 Mins</option>
                            </select>
                        </div>


                        <!-- Create a div where the graph will take place -->
                        <div id="my_dataviz" style="width: 100%;"></div>

                        <!-- Create a div for the legend -->
                        <div class="legend">
                            <div class="legend-container">
                                <div class="legend-item">
                                    <div class="legend-color" style="background-color: maroon;"></div>
                                    Anomalous
                                </div>
                                <div class="legend-item">
                                    <div class="legend-color" style="background-color: gray;"></div>
                                    Active
                                </div>
                                <div class="legend-item">
                                    <div class="legend-color" style="background-color: silver;"></div>
                                    Inactive
                                </div>
                            </div>
                        </div>

                        <br/>

                    </div>
                </div>
            </div>

            <div class="row">

                <div class="col-6">
                    <div class="card">
                       <div class="filter" style="margin-right:15px;">
                            <div class="input-group">
                                <input type="text" class="form-control form-control-sm" placeholder="Search..">
                                <button class="btn btn-outline-secondary btn-sm" type="button">
                                    <i class="fas fa-search"></i>
                                </button>
                            </div>
                            <br/>
                            <select class="form-select form-select-sm" style="float: right;">
                                <option>PAST 1 Min</option>
                                <option>PAST 5 Mins</option>
                                <option>PAST 10 Mins</option>
                            </select>
                       </div>
                       <div class="card-body">
                          <h5 class="card-title">Machine Level Anomaly Ratio</h5>
                          <canvas id="chart1" style="margin:auto; min-height: 130px;"></canvas>
                       </div>
                    </div>
                </div>


                <div class="col-6">
                    <div class="card">
                        <div class="card-body">
                          <h5 class="card-title">Overall Level Anomaly Ratio</h5>
                          <canvas id="chart2" style="margin:auto; min-height: 130px;"></canvas>
                        <div>
                    </div>
                </div>

            </div>
        </div>
	</section>
</main> 
</body>
</html>