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
        hideLoader();

         // Function to show the loader
         function showLoader() {
            $('.loader').addClass('active');
            $('#content-to-blur').css('filter', 'blur(4px)');
        }

        // Function to hide the loader
        function hideLoader() {
            $('.loader').removeClass('active');
            $('#content-to-blur').css('filter', 'none');
        }

        $('#topLevelTimeFilter').on('change', function () {

            showLoader();

            $.ajax({
                url: './anomaly.do',
                type: 'POST',
                dataType: 'text json',
                data: { action: 'topLevelStats', interval: this.value },
                beforeSend: function () { },
                complete: function (xhr, status) { },
                success: function (response) {
                    hideLoader();
                    console.log("topLevelStats success");
                    //console.log(JSON.stringify(response));
                    let topLevelStatsData = response['data'];
                    if (topLevelStatsData === '[]' || topLevelStatsData == null) {
                        //console.log('topLevelStatsData ' + topLevelStatsData);
                        $('#heatmap_container').hide();
                        $('#noDataHeatmap').show();
                    } else {
                        $('#noDataHeatmap').hide();
                        $('#heatmap_container').show();
                        topLevelStatsData = JSON.parse(topLevelStatsData);
                        //console.log(JSON.stringify(topLevelStatsData));
                        updateTopLevelStatsChart(topLevelStatsData,response['prevTime'],response['startTime'])
                    }
                },
                error: function (xhr, status, error) {
                    // Handle errors here
                    console.error('Error:', error);
                }
            });


            $.ajax({
                url: './anomaly.do',
                type: 'POST',
                dataType: 'text json',
                data: { action: 'machineLevelAnomaly', interval: this.value, hostname: $('#machineNameList').val() },
                beforeSend: function () { },
                complete: function (xhr, status) { },
                success: function (response) {
                    console.log("machineLevelAnomaly success");
                    //console.log(JSON.stringify(response));
                    let machineLevelAnomalyRatioData = response['data'];
                    if (machineLevelAnomalyRatioData === '[]' || machineLevelAnomalyRatioData == null) {
                        //console.log('machineLevelAnomalyRatioData ' + machineLevelAnomalyRatioData);
                        $('#machineLevelAnomaly_container').hide();
                        $('#noDataScatter').show();
                    } else {
                        $('#machineLevelAnomaly_container').show();
                        $('#noDataScatter').hide();
                        machineLevelAnomalyRatioData = JSON.parse(machineLevelAnomalyRatioData);
                        //console.log(JSON.stringify(machineLevelAnomalyRatioData));
                        updateMachineLevelAnomaly(machineLevelAnomalyRatioData,response['prevTime'],response['startTime']);
                    }
                },
                error: function (xhr, status, error) {
                    // Handle errors here
                    console.error('Error:', error);
                }
            });

        });

        $('#machineNameList').on('change', function () {
            showLoader();
            $.ajax({
                url: './anomaly.do',
                type: 'POST',
                dataType: 'text json',
                data: { action: 'machineLevelAnomaly', interval: $('#topLevelTimeFilter').val(), hostname: this.value },
                beforeSend: function () { },
                complete: function (xhr, status) { },
                success: function (response) {
                    hideLoader();
                    console.log("machineLevelAnomaly success");
                    //console.log(JSON.stringify(response));
                    let machineLevelAnomalyRatioData = response['data'];
                    if (machineLevelAnomalyRatioData === '[]' || machineLevelAnomalyRatioData == null) {
                        //console.log('machineLevelAnomalyRatioData ' + machineLevelAnomalyRatioData);
                        $('#machineLevelAnomaly_container').hide();
                        $('#noDataScatter').show();
                    } else {
                        $('#machineLevelAnomaly_container').show();
                        $('#noDataScatter').hide();
                        machineLevelAnomalyRatioData = JSON.parse(machineLevelAnomalyRatioData);
                        //console.log(JSON.stringify(machineLevelAnomalyRatioData));
                        updateMachineLevelAnomaly(machineLevelAnomalyRatioData,response['prevTime'],response['startTime']);
                    }
                },
                error: function (xhr, status, error) {
                    // Handle errors here
                    console.error('Error:', error);
                }
            });
        });


        //var data = [{"hostname":"Win-10-VM","anomaly":false,"time":"2023-09-05T16:30:06.719Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.703Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.532Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.700Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.532Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:03.701Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.685Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.681Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.683Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.681Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.683Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.683Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:03.679Z"},{"hostname":"Win-11-VM","anomaly":true,"time":"2023-09-05T16:30:02.679Z"},{"hostname":"Win-12-VM","anomaly":true,"time":"2023-09-05T16:30:01.679Z"},{"hostname":"Win-13-VM","anomaly":true,"time":"2023-09-05T16:30:03.679Z"},{"hostname":"Win-14-VM","anomaly":true,"time":"2023-09-05T16:30:03.679Z"}];

        let topLevelStatsData = '<bean:write name="anomalyReportForm" property="topLevelStats"/>';
        let startTime = '<bean:write name="anomalyReportForm" property="topLevelStatsCurrentTime"/>';
        let prevTime = '<bean:write name="anomalyReportForm" property="topLevelStatsPrevTime"/>';
        console.log("topLevelStats startTime = "+startTime);
        console.log("topLevelStats prevTime = "+prevTime);
        if (topLevelStatsData === '[]' || topLevelStatsData == null) {
            console.log('topLevelStatsData ' + topLevelStatsData);
            $('#heatmap_container').hide();
            $('#noDataHeatmap').show();
        } else {
            topLevelStatsData = JSON.parse(topLevelStatsData.replace(/&quot;/g, '"'));
            console.log(topLevelStatsData);
            loadTopLevelStatsChart(topLevelStatsData, prevTime, startTime);
        }

        let machineNameList = '<bean:write name="anomalyReportForm" property="machineNameList"/>';

        if (machineNameList === '[]' || machineNameList == null) {
        } else {
            machineNameList = JSON.parse(machineNameList.replace(/&quot;/g, '"'));

            // Select the <select> element by its ID
            var machineNameSelect = $('#machineNameList');

            // Use a lambda function to append each option to the select element
            $.each(machineNameList, (index, option) => {
                machineNameSelect.append($('<option>', {
                    value: option.hostname,
                    text: option.hostname
                }));
            });
        }


        let machineLevelAnomalyRatioData = '<bean:write name="anomalyReportForm" property="machineLevelAnomaly"/>';
        if (machineLevelAnomalyRatioData === '[]' || machineLevelAnomalyRatioData == null) {
            console.log('machineLevelAnomalyRatioData ' + machineLevelAnomalyRatioData);
            $('#machineLevelAnomaly_container').hide();
            $('#noDataScatter').show();
        } else {
            machineLevelAnomalyRatioData = JSON.parse(machineLevelAnomalyRatioData.replace(/&quot;/g, '"'));
            console.log(machineLevelAnomalyRatioData)
            loadMachineLevelAnomaly(machineLevelAnomalyRatioData, prevTime, startTime);
        }
    });

    function updateTopLevelStatsChart(data, prevTime, startTime) {

        d3.select("#heatmap")
            .select("svg")
            .remove();

        loadTopLevelStatsChart(data, prevTime, startTime);
    }

    function loadTopLevelStatsChart(data, prevTime, startTime) {

        const timeArray = Array.from(new Set(data.map(d => d.time)))

        // Define a custom time format for tooltip
        const customTimeFormat = d3.timeFormat("%d %B, %H:%M:%S"); // Customize the format as needed

        var numCols = timeArray.length;

        var parentWidth = document.getElementById("heatmap_container").offsetWidth;
        var h = new Set(data.map(d => d.hostname)).size * 20 + 40 + 50;

        // set the dimensions and margins of the graph
        const margin = { top: 40, right: 25, bottom: 50, left: 100 },
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
            .domain([new Date(prevTime), new Date(startTime)]);

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
            .call(d3.axisBottom(x).tickSize(0).tickFormat(d3.timeFormat("%H:%M:%S")));

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

    function updateMachineLevelAnomaly(data, prevTime, startTime) {

        d3.select("#machineLevelAnomaly")
            .select("svg")
            .remove();

        loadMachineLevelAnomaly(data, prevTime, startTime);
    }

    function loadMachineLevelAnomaly(data, prevTime, startTime ) {

        //const timeArray = Array.from(new Set(data.map(d => d.time)))

        // Define a custom time format for tooltip
        const customTimeFormat = d3.timeFormat("%d %B, %H:%M:%S"); // Customize the format as needed

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
            .domain([new Date(prevTime), new Date(startTime)])
            .range([0, width]);

        const yScale = d3.scaleBand()
            .domain(data.map(d => d.event_id))
            .range([height, 0])
            .padding(0.1);

        // Create x and y axis
        const xAxis = d3.axisBottom(xScale).tickSize(-height).tickFormat(d3.timeFormat("%H:%M:%S"));
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
            .text("Event IDs");


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
        fill: silver;
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

    .loader {
        position: fixed;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(255, 255, 255, 0.7);
        display: none;
        justify-content: center;
        align-items: center;
        z-index: 9999;
    }

    .loader .spinner-border {
        width: 3rem;
        height: 3rem;
        filter: none;
    }

    #content-to-blur {
        filter: blur(4px);
        transition: filter 0.3s;
    }

    .loader.active {
        display: flex;
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
		
		<section class="section dashboard" id="content-to-blur">

            <div class="loader">
                <div class="spinner-border" role="status">
                    <span class="sr-only">Loading...</span>
                </div>
            </div>

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

                            <select id="topLevelOsFilter" class="form-select form-select-sm">
                                <option value="all">All OS</option>
                                <option value="windows">Windows</option>
                                <option value="linux">Linux</option>
                            </select>

                            <select class="form-select form-select-sm">
                                <option value="item1">STATUS</option>
                                <option value="item2">Anomalous</option>
                                <option value="item3">Active</option>
                                <option value="item3">Inactive</option>
                            </select>
                        </div>
                        <div style="float: right;">
                            <select id="topLevelTimeFilter" class="form-select form-select-sm">
                                <option value="1">PAST 1 Min</option>
                                <option value="2" selected>PAST 2 Mins</option>
                                <option value="5">PAST 5 Mins</option>
                                <option value="10">PAST 10 Mins</option>
                                <option value="15">PAST 15 Mins</option>
                                <option value="20">PAST 20 Mins</option>
                                <option value="30">PAST 30 Mins</option>
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
                        <h5 class="card-title" style="margin: 0;">Machine Level Anomaly <span data-bs-toggle="tooltip" data-bs-placement="right" title="This graph shows the datapoint where Anomalies were detected at particular Time for particular Event."><i
                            class="fa-solid fa-circle-info text-primary"></i></span></h5>
                        <div style="display: inline-flex;">
                            <label for="machineNameList" class="col-form-label">Machine: </label>
                            <select id="machineNameList" class="form-select form-select-sm">
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