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


<script type="text/javascript">

$(function () {

    $('#anomalyReport').addClass('nav-selected');

    //var data = [{"hostname":"Win-10-VM","anomaly":false,"time":"2023-09-05T16:30:06.719Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.703Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.533Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.532Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.700Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:04.532Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:03.701Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.685Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:02.525Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.681Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.683Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.681Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.683Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.683Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:01.519Z"},{"hostname":"Win-10-VM","anomaly":true,"time":"2023-09-05T16:30:03.679Z"},{"hostname":"Win-11-VM","anomaly":true,"time":"2023-09-05T16:30:02.679Z"},{"hostname":"Win-12-VM","anomaly":true,"time":"2023-09-05T16:30:01.679Z"},{"hostname":"Win-13-VM","anomaly":true,"time":"2023-09-05T16:30:03.679Z"},{"hostname":"Win-14-VM","anomaly":true,"time":"2023-09-05T16:30:03.679Z"}];


    $.ajax({
        url: './anomaly.do',
        type: 'POST',
        dataType: 'text json',
        data: {action: 'heatmapData'},
        beforeSend: function() {},
        complete: function (xhr, status) {},
        success: function (response) {
            console.log("succuess");
            console.log(JSON.stringify(response));
            loadScatterChart(response);
        }
    });


    //loadScatterChart(data);

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

function loadScatterChart(jsonData) {
    // Extract the data into separate arrays for true and false anomalies
    var trueAnomalies = [];
    var falseAnomalies = [];
    
    var xMin = new Date(jsonData[0].time);
    var xMax = xMin;

    jsonData.forEach(function (item) {
        
        xMin = xMin < new Date(item.time) ? xMin : new Date(item.time);
        xMax = xMax > new Date(item.time) ? xMax : new Date(item.time);
    
        if (item.anomaly) {
            trueAnomalies.push({ x: new Date(item.time), y: item.hostname });
        } else {
            falseAnomalies.push({ x: new Date(item.time), y: item.hostname });
        }
    });
    
    xMin = xMin.setSeconds(xMin.getSeconds() - 1)
    yMin = xMax.setSeconds(xMax.getSeconds() + 1)
    
    console.log("xMin = "+xMin)
    console.log("xMax = "+xMax)

    var ctx = document.getElementById('scatterChart').getContext('2d');

    // Create the scatter chart
    var scatterChart = new Chart(ctx, {
        type: 'scatter',
        data: {
            datasets: [
                {
                    label: 'Anomalies Detected',
                    data: trueAnomalies,
                    backgroundColor: 'rgba(255, 0, 0, 0.5)', // Red color for true anomalies
                    pointRadius: 5 // Adjust point size as needed
                },
                {
                    label: 'Anomalies Not Detected',
                    data: falseAnomalies,
                    backgroundColor: 'rgb(129, 133, 137)', // Gray color for false anomalies
                    pointRadius: 5 // Adjust point size as needed
                }
            ]
        },
        options: {
            scales: {
                x: {
                    type: 'time',
                    time: {
                        unit: 'second',
                        parser: 'moment', // Use Moment.js for parsing dates
                        tooltipFormat: 'HH:mm:ss' // Customize the tooltip date format as needed
                    },
                    position: 'bottom',
                    title: {
                        display: true,
                        text: 'Time'
                    },
                    min: xMin,
                    max: xMax
                },
                y: {
                    type: 'category',
                    position: 'left',
                    title: {
                        display: true,
                        text: 'Hostname'
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

</script>

<style>

    .form-select {
        width: fit-content; 
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
                                <option>PAST 5 Mins</option>
                                <option>PAST 10 Mins</option>
                            </select>
                        </div>

                        <!-- Create a div where the graph will take place -->
                        <canvas id="scatterChart" width="400" height="50" style="margin-top: 20px; margin-bottom: 20px;"></canvas>

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