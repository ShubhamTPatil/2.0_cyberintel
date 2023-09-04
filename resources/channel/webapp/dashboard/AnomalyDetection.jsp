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


<!-- Load d3.js -->
<script src="https://d3js.org/d3.v6.js"></script>


<style>

.dropdown {
            width: 120px;
            margin-bottom: 5px;
            border: none;
            border-bottom: 1px solid #ccc;
            padding:5px;
            position:absolute;
            top:4;
            left:80%;
        }
        .with-danger {
            border: 1px solid blue;
            background-color: whitesmoke;
            
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
						title="Anamoly Detection"><i
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
		

		
		<section class="section anamolyDetection">
		
		<div class="card">
		           <div class="card-body">
		              <span class="card-title">Top Level Statistics</span>
		              <span
						data-bs-toggle="tooltip" data-bs-placement="right"
						title="Anamoly Detection"><i
						class="fa-solid fa-circle-info text-primary"></i></span>
		              <div>
		</div> 
		
		</br> 
		
	<!-- Scatter plot  -->
		<div class="card" style="width: 100%; display: inline-block; margin-right: 20px;">
        <div class="card-body">
            <h5 class="card-title">Event Data Monitoring</h5>
                                   <div>
                                                <select class="dropdown with-danger">
                                                    <!-- Dropdown options -->
                                                    <option>PAST 1 Min</option>
                                                    <option>PAST 5 Mins</option>
                                                    <option>PAST 10 Mins</option>
                                                </select>
                                                </div>
                          <div class="card" style="border: 1px solid #ccc;
                                                    height: 40px;
                                                    width: 380px;
                                                    border-radius: 15px;
                                                    display: inline-block;
                                                    background-color: whitesmoke;
                                                    padding-top:10px;
                                                    /* Rounded corners */ ">



                                    <select id="dropdown1"
                                                        style="border-radius:15px; width:130px;border: 2px solid white; padding-center: 5px; color:skyblue;">
                                                        <option value="optionA" style="color:black;">APPLICATION</option>
                                                        <option value="optionA" style="color:black;">All</option>
                                                        <option value="option1" style="color:black;">Application 1</option>
                                                        <option value="option2" style="color:black;">Application 2</option>
                                                        <option value="option3" style="color:black;">Application 3</option>
                                                    </select>

                                                    <select id="dropdown2"
                                                        style="border-radius:15px; width:100px;border:none;">
                                                        <option value="option1">SUBNETS</option>
                                                        <option value="optionA">127.0.0.1</option>
                                                        <option value="optionB">127.0.0.2</option>
                                                        <option value="optionC">255.255.0.1</option>
                                                    </select>

                                                    <select id="dropdown3"
                                                        style="border-radius:15px;width:50px; border:none;">
                                                        <option value="choice1">OS</option>
                                                        <option value="choiceX">Windows</option>
                                                        <option value="choiceY">Linux</option>
                                                     
                                                    </select>

                                                    <select id="dropdown4"
                                                        style="border-radius:15px;width:80px;border:none;">
                                                        <option value="item1">STATUS</option>
                                                        <option value="item2">Anomalous</option>
                                                        <option value="item3">Active</option>
                                                        <option value="item3">Inactive</option>
                                                    </select>
                                         </div>
    
                         <!-- Create a div where the graph will take place -->
    <div id="my_dataviz"></div>
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


        </div>
        
    </div> 
    
 
		 
	</section>
	
	<section class= "section pieChart">
	
	<div class="card">
		           <div class="card-body">
		              <span class="card-title">Machine Level Statistics</span>
		              <span
						data-bs-toggle="tooltip" data-bs-placement="right"
						title="Anamoly Detection"><i
						class="fa-solid fa-circle-info text-primary"></i></span>
		              <div>
		</div>
		</br> 
		                
     <!-- First chart card -->
    <div class="card" style="width: 600px; display: inline-block; margin-right: 20px;">
        <div class="card-body">
            <h5 class="card-title">Machine Level Anomaly Ratio</h5>
               <div class="col-md-4">
                       <div class="input-group">
                           <input type="text" class="form-control" placeholder="Search..." aria-label="Search" style="height:30px;">
                               <div class="input-group-append">
                                  <span class="input-group-text" style="height:30px;"><i class="fa fa-search"></i></span>
                                </div>
                        </div>
                </div>
                
                <div class="btn-group" style="left:40%;bottom:70%;position:absolute;height:30px;"> 
                       <button type="button" class="btn btn-info">Status</button> 
                       <button type="button" class="btn btn-info dropdown-toggle dropdown-toggle-split" data-toggle="dropdown"> </button>
                               <div class="dropdown-menu">
                                     <a class="dropdown-item" href="#">PAST 1 Min</a> 
                                     <a class="dropdown-item" href="#">PAST 5 Mins</a> 
                                     <a class="dropdown-item" href="#">PAST 10 Mins</a> 
                                </div>
                 </div>

            <canvas id="chart1" width="300" height="200"></canvas>
        </div>
    </div>

    <!-- Second chart card -->
    <div class="card" style="width: 450px; display:inline-block;">
        <div class="card-body">
            <h5 class="card-title">Overall Level Anomaly Ratio</h5>
            <canvas id="chart2" width="300" height="200"></canvas>
        </div>
    </div>
    
</section>
  
 <script>


 // Set dimensions and margins
 const margin = { top: 50, right: 50, bottom: 70, left: 150 };
 const width = 900 - margin.left - margin.right;
 const height = 500 - margin.top - margin.bottom;

 // Append SVG element
 const svg = d3.select("#my_dataviz")
     .append("svg")
     .attr("width", width + margin.left + margin.right)
     .attr("height", height + margin.top + margin.bottom)
     .append("g")
     .attr("transform", `translate(${margin.left},${margin.top})`);

 // Sample data
 const numRows = 20;
 const numCols = 60;
 const data = [];
 const startTime = new Date(2023, 0, 1).getTime();
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


 
// Sample data (replace this with your actual data)
var chart1Data = {
    //labels: ["Type 1", "Type 2", "Type 3", "Type 4"],
    datasets: [{
        data: [30, 40, 15, 15],
        backgroundColor: ['#4A87B5', '#49A5DE', '#71DCEB', '#7D8EDF'],
    }]
};

var chart2Data = {
    //labels: ["Type 1", "Type 2", "Type 3", "Type 4"],
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
</script>
</main> 
</body>
</html>