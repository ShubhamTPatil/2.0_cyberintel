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
<script type="text/javascript" src="/spm/js/newdashboard/chartjs.moment.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/chartjs-adapter-moment.min.js"></script>
<script type="text/javascript" src="/spm/js/newdashboard/d3.v6.min.js"</script></script>

<script type="text/javascript">

$(function () {

    $('#anomalyReport').addClass('nav-selected');

    //var data = [{"hostname":"Win-10-VM","anomaly":false,"time":"2023-09-05T16:30:06.719Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.703Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.532Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.700Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.532Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:03.701Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.685Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.681Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.683Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.681Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.683Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.683Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:03.679Z"},{"hostname":"Win-11-VM","anomaly":true,"time":"2023-09-05T16:30:02.679Z"},{"hostname":"Win-12-VM","anomaly":true,"time":"2023-09-05T16:30:01.679Z"},{"hostname":"Win-13-VM","anomaly":true,"time":"2023-09-05T16:30:03.679Z"},{"hostname":"Win-14-VM","anomaly":true,"time":"2023-09-05T16:30:03.679Z"}];

      let topLevelStatsData = '<bean:write name="anomalyReportForm" property="topLevelStats"/>';
      //loadScatterChart(JSON.parse(topLevelStatsData.replace(/&quot;/g,'"')));

      if(topLevelStatsData === '[]' || topLevelStatsData == null){
        console.log('topLevelStatsData '+topLevelStatsData);
        $('#heatmap_container').hide();
        $('#noDataHeatmap').show();
      } else {
        topLevelStatsData = JSON.parse(topLevelStatsData.replace(/&quot;/g,'"'));
        loadHeatMap(topLevelStatsData);
      }

      let machineLevelAnomalyRatioData = '<bean:write name="anomalyReportForm" property="machineLevelAnomalyPieChartData"/>';
      if(machineLevelAnomalyRatioData === '[]' || machineLevelAnomalyRatioData == null){
        console.log('machineLevelAnomalyRatioData '+machineLevelAnomalyRatioData);
        $('#machineLevelAnomaly_container').hide();
        $('#noDataScatter').show();
      } else {
        machineLevelAnomalyRatioData = JSON.parse(machineLevelAnomalyRatioData.replace(/&quot;/g,'"'));
        loadMachineLevelAnomaly(machineLevelAnomalyRatioData);
      }

   /* $.ajax({
        url: './anomaly.do',
        type: 'POST',
        dataType: 'text json',
        data: {action: 'heatmapData' , interval: 5, os : 'windows'},
        beforeSend: function() {},
        complete: function (xhr, status) {},
        success: function (response) {
            console.log("success");
            console.log(JSON.stringify(response));
            loadScatterChart(response);
        },
        error: function(xhr, status, error) {
            // Handle errors here
            console.error('Error:', error);
        }
    });*/
});

function loadHeatMap(data) {

const timeArray = Array.from(new Set(data.map(d => d.time)))

// Define a custom time format for tooltip
const customTimeFormat = d3.timeFormat("%d %B, %H:%M:%S"); // Customize the format as needed


var startTime = d3.min(timeArray);
var endTime = d3.max(timeArray);

var numCols = timeArray.length;

var parentWidth = document.getElementById("heatmap_container").offsetWidth;
var h = new Set(data.map(d => d.hostname)).size * 20 + 40 + 50;

// set the dimensions and margins of the graph
const margin = {top: 40, right: 25, bottom: 50, left: 100},
  width = parentWidth - margin.left - margin.right,
  height = h - margin.top - margin.bottom;

// append the svg object to the body of the page
const svg = d3.select("#heatmap")
.append("svg")
  .attr("width", width + margin.left + margin.right)
  .attr("height", height + margin.top + margin.bottom)
.append("g")
  .attr("transform", `translate(${margin.left}, ${margin.top})`);

     // Create scales
     const x = d3.scaleTime()
         .range([0, width])
         .domain([new Date(startTime), new Date(endTime)]);

     const y = d3.scaleBand()
         .range([height, 0])
         .domain(data.map(d => d.hostname))
         .padding(0.01);

     // Calculate cell width based on time interval
     const cellWidth = width / numCols;

     // Render the heatmap
     const cells = svg.selectAll("rect")
         .data(data.flat())
         .enter().append("rect")
         .attr("x", d => x(new Date(d.time)))
         .attr("y", (d) => y(d.hostname))
         .attr("width", cellWidth)
         .attr("height", y.bandwidth())
         .attr("class", d => `cell-${d.anomaly}`) // Add class based on state
         .attr("stroke", "white") // Add white stroke for cell borders
         .style("stroke-width", 1) // Set border width
         .on('mouseover', function (event, d) {
             const tooltip = document.getElementById("tooltip");
             tooltip.innerHTML = `Anomaly: ${d.anomaly}<br>Machine: ${d.hostname}<br>Time: ${customTimeFormat(new Date(d.time))}`;

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
         .style("background-color", "white")
         .style("padding", "10px")
         .style("border", "1px solid #ccc")
         .style("border-radius", "5px")
         .style("display", "none");

     // Add x-axis (time series)
     svg.append("g")
         .attr("transform", `translate(0,${height})`)
         .call(d3.axisBottom(x).tickSize(0));

     // Add y-axis (machine names)
     svg.append("g")
         .call(d3.axisLeft(y).tickSize(0));

     // Apply CSS style to hide the axis line (stroke)
     svg.selectAll("path")
        .style("display", "none");

     // Add x-axis label
     svg.append("text")
         .attr("class", "axis-label")
         .attr("x", width / 2)
         .attr("y", height + margin.bottom - 5)
         .style("text-anchor", "middle")
         .text("Time");

     // Add y-axis label
     svg.append("text")
         .attr("class", "axis-label")
         .attr("transform", "rotate(-90)")
         .attr("x", -height / 2)
         .attr("y", -margin.left + 20)
         .style("text-anchor", "middle")
         .text("Machines");

}



function loadScatterChart(jsonData) {
    // Extract the data into separate arrays for true and false anomalies
    var trueAnomalies = [];
    var falseAnomalies = [];
    var notTrainedAnomalies = [];
    
    var xMin = new Date(jsonData[0].time);
    var xMax = xMin;

    jsonData.forEach(function (item) {
        
        xMin = xMin < new Date(item.time) ? xMin : new Date(item.time);
        xMax = xMax > new Date(item.time) ? xMax : new Date(item.time);
    
        if (item.anomaly === "true") {
            trueAnomalies.push({ x: new Date(item.time), y: item.event_id });
        } else if(item.anomaly === "false") {
            falseAnomalies.push({ x: new Date(item.time), y: item.event_id });
        } else if(item.anomaly === "Not_Trained") {
            notTrainedAnomalies.push({ x: new Date(item.time), y: item.event_id });
        } else {

        }
    });
    
    //xMin = xMin.setSeconds(xMin.getSeconds() - 1)
    //yMin = xMax.setSeconds(xMax.getSeconds() + 1)

    console.log("xMin = "+xMin)
    console.log("xMax = "+xMax)

    var ctx = document.getElementById('scatterChart').getContext('2d');

    // Create the scatter chart
    var scatterChart = new Chart(ctx, {
        type: 'scatter',
        data: {
            datasets: [
                {
                    label: 'Anomalous',
                    data: trueAnomalies,
                    backgroundColor: 'rgba(255, 0, 0, 0.5)', // Red color for true anomalies
                    pointRadius: 6 // Adjust point size as needed
                },
                {
                    label: 'Active',
                    data: falseAnomalies,
                    backgroundColor: 'rgb(129, 133, 137)', // Gray color for false anomalies
                    pointRadius: 6 // Adjust point size as needed
                },
                {
                    label: 'Not Trained',
                    data: notTrainedAnomalies,
                    backgroundColor: 'rgb(128,0,128)', // Gray color for false anomalies
                    pointRadius: 6 // Adjust point size as needed
                }
            ]
        },
        options: {
            scales: {
                x: {
                    type: 'time',
                    position: 'bottom',
                    title: {
                        display: true,
                        text: 'Time'
                    },
                    min: xMin,
                    max: xMax,
                },
                y: {
                    type: 'category',
                    position: 'left',
                    title: {
                        display: true,
                        text: 'Events'
                    }
                }
            },
            plugins: {
                legend: {
                    display: true,
                    position: 'bottom'
                }
            }
        }
    });
}



    function loadMachineLevelAnomaly(data) {

        const timeArray = Array.from(new Set(data.map(d => d.time)))

        // Define a custom time format for tooltip
        const customTimeFormat = d3.timeFormat("%d %B, %H:%M:%S"); // Customize the format as needed

        var startTime = d3.min(timeArray);
        var endTime = d3.max(timeArray);

        var parentWidth = document.getElementById("machineLevelAnomaly_container").offsetWidth;
        var h = new Set(data.map(d => d.event_id)).size * 20 + 40 + 50;

        // set the dimensions and margins of the graph
        const margin = { top: 40, right: 25, bottom: 50, left: 100 },
            width = parentWidth - margin.left - margin.right,
            height = h - margin.top - margin.bottom;

        // append the svg object to the body of the page
        const svg = d3.select("#machineLevelAnomaly")
            .append("svg")
            .attr("width", width + margin.left + margin.right)
            .attr("height", height + margin.top + margin.bottom)
            .append("g")
            .attr("transform", `translate(${margin.left}, ${margin.top})`);

        // Create x and y scales
        const xScale = d3.scaleTime()
            .domain([new Date(startTime), new Date(endTime)])
            .range([0, width]);

        const yScale = d3.scaleBand()
            .domain(data.map(d => d.event_id))
            .range([height, 0])
            .padding(0.1);

        // Create x and y axis
        const xAxis = d3.axisBottom(xScale).tickSize(-height);
        const yAxis = d3.axisLeft(yScale).tickSize(-width);

        // Append x and y axis elements to the SVG
        svg.append('g')
            .attr('class', 'x-axis')
            .attr('transform', `translate(0, ${height})`)
            .call(xAxis);

        svg.append('g')
            .attr('class', 'y-axis')
            .call(yAxis);

        // Style the axis grids
        svg.selectAll('.x-axis .tick line')
            .attr('class', 'grid-line');

        svg.selectAll('.y-axis .tick line')
            .attr('class', 'grid-line');

        // Add grid lines to the chart
        svg.selectAll('.grid-line')
            .attr('stroke', 'lightgray')
            .attr('stroke-dasharray', '4 4');

        svg.selectAll('.circle')
            .data(data.flat())
            .enter()
            .append('circle')
            .attr('class', 'circle')
            .attr('cx', d => xScale(new Date(d.time))) // x-coordinate based on time
            .attr('cy', d => yScale(d.event_id) + yScale.bandwidth() / 2) // y-coordinate based on string position
            .attr('r', d => 3) // radius of the circle based on circleValue
            .attr("class", d => `cell-${d.anomaly}`) // Add class based on state
            .on('mouseover', function (event, d) {
                const tooltip = document.getElementById("mtooltip");
                tooltip.innerHTML = `Anomaly: ${d.anomaly}<br>Event ID: ${d.event_id}<br>Time: ${customTimeFormat(new Date(d.time))}`;

                tooltip.style.display = "block";
                tooltip.style.left = (event.pageX + 10) + "px";
                tooltip.style.top = (event.pageY + 10) + "px";
            })
            .on('mouseout', function () {
                document.getElementById("mtooltip").style.display = "none";
            });

        // Add tooltips
        const tooltip = d3.select("body")
            .append("div")
            .attr("id", "mtooltip")
            .style("position", "absolute")
            .style("z-index", "10")
            .style("background-color", "white")
            .style("padding", "10px")
            .style("border", "1px solid #ccc")
            .style("border-radius", "5px")
            .style("display", "none");

        
        // Add x-axis label
        svg.append("text")
            .attr("class", "axis-label")
            .attr("x", width / 2)
            .attr("y", height + margin.bottom - 5)
            .style("text-anchor", "middle")
            .text("Time");

        // Add y-axis label
        svg.append("text")
            .attr("class", "axis-label")
            .attr("transform", "rotate(-90)")
            .attr("x", -height / 2)
            .attr("y", -margin.left + 20)
            .style("text-anchor", "middle")
            .text("Events");

    }

</script>

<style>

    .form-select {
        width: fit-content; 
    }

    /* Define styles for different cell states */
    .cell-true {
        fill: maroon;
    }

    .cell-false {
        fill: gray;
    }

    .cell-Not_Trained {
        fill: purple;
    }

    /* Define styles for legend */
    .legend {
        display: flex;
        justify-content: flex-start;
        margin-top: -5px;
        margin-left: 100px;
    }

    .legend-item {
        display: inline-flex;
        align-items: center;
        margin-right: 5px;
    }
    .legend-color {
        width: 20px;
        height: 12px;
        margin-right: 5px;
    }

    .no-data {
        font-size: large; 
        display: none; 
        text-align: center; 
        margin-top: 10px;
    }

</style>


</head>
<body>
<html:form name ="anomalyReportForm" action="/anomaly.do" type="com.marimba.apps.subscriptionmanager.webapp.bean.anomaly.pojo.AnomalyReportForm">
<html:hidden property="action"/>

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
                    <div class="card-body">
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
                                <option selected>PAST 2 Mins</option>
                                <option>PAST 5 Mins</option>
                                <option>PAST 10 Mins</option>
                            </select>
                        </div>

                        <div id="noDataHeatmap" class="no-data">NO DATA AVAILABLE!</div>

                        <div id="heatmap_container" style="width:100%;">
                            <!-- Create a div where the graph will take place -->
                            <div id="heatmap"></div>

                            <!-- Create a div for the legend -->
                            <div class="legend">
                            	<div class="legend-container">
                            		<div class="legend-item">
                            			<div class="legend-color cell-true" style="background-color: maroon;"></div>
                            			Anomalous
                            		</div>
                            		<div class="legend-item">
                            			<div class="legend-color cell-false" style="background-color: silver;"></div>
                            			Active
                            		</div>
                            		<div class="legend-item">
                            			<div class="legend-color cell-Not_Trained" style="background-color: purple;"></div>
                            			Not Trained
                            		</div>
                            	</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="row">
                <div class="card info-card">
                    <div class="card-body">
                        <h5 class="card-title" style="margin: 0;">Machine Level Anomaly</h5>

                        <div style="display: inline-flex;">
                            <!-- <label for="machineName" class="col-form-label"></label> -->
                            <span id="machineName">Machine: Win-10-VM</span>
                            <!-- <input type="text" id="machineName" class="form-control" aria-describedby="machineName" value="Win-10-VM"> -->
                        </div>

                        <div style="float: right;">
                            <select class="form-select form-select-sm">
                                <option>PAST 1 Min</option>
                                <option selected>PAST 2 Mins</option>
                                <option>PAST 5 Mins</option>
                                <option>PAST 10 Mins</option>
                            </select>
                        </div>

                        <div id="noDataScatter" class="no-data">NO DATA AVAILABLE!</div>

                        <div id="machineLevelAnomaly_container" style="width:100%;">
                            <!-- Create a div where the graph will take place -->
                            <div id="machineLevelAnomaly"></div>

                            <!-- Create a div for the legend -->
                            <div class="legend">
                            	<div class="legend-container">
                            		<div class="legend-item">
                            			<div class="legend-color cell-true" style="background-color: maroon;"></div>
                            			Anomalous
                            		</div>
                            		<div class="legend-item">
                            			<div class="legend-color cell-false" style="background-color: silver;"></div>
                            			Active
                            		</div>
                            		<div class="legend-item">
                            			<div class="legend-color cell-Not_Trained" style="background-color: purple;"></div>
                            			Not Trained
                            		</div>
                            	</div>
                            </div>
                        </div>

                        <!-- <canvas id="scatterChart" width="400" height="100" style="margin-top: 20px; margin-bottom: 20px;"></canvas> -->
                    </div>
                </div>
            </div>
        </div>
	</section>
</main>
</html:form>
</body>
</html>