<%--
// Copyright 2022-2023, Harman International. All Rights Reserved.
// Confidential and Proprietary Information of Harman International.
<!-- Author: Nandakumar Sankaralingam -->
--%>
<%@ page
	import="com.marimba.apps.subscription.common.ISubscriptionConstants"
	contentType="text/html;charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/webapps.tld" prefix="webapps"%>

<!DOCTYPE html>
<html lang="en">

<head>


  <link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap.min.css" />
  <link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/bootstrap-icons.min.css" />
  <link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/all.min.css" />
  <link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/datatables.min.css" />
  <link rel="stylesheet" type="text/css" href="/spm/css/newdashboard/style.css" />

  <script type="text/javascript" src="/spm/js/newdashboard/jquery.min.js"></script>
  <script type="text/javascript" src="/spm/js/newdashboard/bootstrap.bundle.min.js"></script>
  <script type="text/javascript" src="/spm/js/newdashboard/chart.umd.js"></script>
  <script type="text/javascript" src="/spm/js/newdashboard/datatables.min.js"></script>
  <script type="text/javascript" src="/spm/js/newdashboard/all.min.js"></script>
  <script type="text/javascript" src="/spm/js/newdashboard/common.js"></script>

  <script type="text/javascript">

    $(function () {

      $('.nav-selected').removeClass('nav-selected');
      $('#definitionsUpdate').addClass('nav-selected');


    });

  </script>
</head>

<body>


  <jsp:include page="header.jsp" />
  <jsp:include page="sidebar.jsp" />


  <main id="main" class="main">
    <div class="pagetitle">

      <div class="d-flex bd-highlight justify-content-center">
        <div class="p-2 flex-grow-1 bd-highlight">
          <span class="pagename">Definitions Update</span> <span data-bs-toggle="tooltip" data-bs-placement="right"
            title="Definitions Update"><i class="fa-solid fa-circle-info text-primary"></i></span>
        </div>
        <div class="refresh p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip"
          data-bs-placement="right" title="Refresh" style="cursor: pointer;"><i class="fa-solid fa-arrows-rotate"></i>
        </div>
        <div class="p-2 bd-highlight text-primary align-self-center" data-bs-toggle="tooltip" data-bs-placement="right"
          title="Download" style="cursor: pointer;">
          <i class="fa-solid fa-download"></i>
        </div>
        <div class="p-2 bd-highlight text-primary align-self-center">
          <a href="/shell/dashboard.do"><i class="fa-solid fa-chevron-left" style="margin-right: 5px;"></i>CMS
            Home</a>
        </div>
      </div>

    </div>

    <section class="section dashboard">

        <nav>
          <div class="nav nav-tabs" id="nav-tab" role="tablist">
            <button class="nav-link active" id="nav-home-tab" data-bs-toggle="tab" data-bs-target="#nav-home"
              type="button" role="tab" aria-controls="nav-home" aria-selected="true">CVE INFORMATOIN</button>
            <button class="nav-link" id="nav-profile-tab" data-bs-toggle="tab" data-bs-target="#nav-profile"
              type="button" role="tab" aria-controls="nav-profile" aria-selected="false">UPDATES</button>
          </div>
        </nav>


        <div class="tab-content" id="nav-tabContent">
          <div class="tab-pane fade show active" id="nav-home" role="tabpanel" aria-labelledby="nav-home-tab">
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row" style="background-color:#d9edf7!important;">
              <div class="col-md-12">
                <h6><i class="fa-solid fa-circle-info text-primary" style="font-size:15px!important;"></i> CVE
                  Definitions and Vulnarability Definitions are need to be updated every month</h6>
              </div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row" style="box-shadow: 1px 1px 1px #3333333d!important;">
              <div class="col-md-12">
                <div class="row">
                  <div class="col-md-6">
                    <h5>CVE Definitions last updated on(04/12/2022)</h5>
                  </div>
                  <div class="col-md-offset-3 col-md-1"><button type="button"
                      class="btn btn-light btn-md">CANCEL</button></div>
                  <div class="col-md-2"><button type="button" class="btn btn-primary btn-md">UPDATE NOW</button></div>
                </div>
                <div class="row">
                  <div class="col-md-12">(Please ensure all information is upto date for accurate results)</div>
                </div>
                <div class="row">
                  <div class="col-md-12">&nbsp;</div>
                </div>
              </div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row" style="box-shadow: 1px 1px 1px #3333333d!important;">
              <div class="col-md-12">
                <div class="row">
                  <div class="col-md-6">
                    <h5>Vulnarability Definitions last updated on(04/12/2022)</h5>
                  </div>
                  <div class="col-md-offset-3 col-md-1"><button type="button"
                      class="btn btn-light btn-md">CANCEL</button></div>
                  <div class="col-md-2"><button type="button" class="btn btn-primary btn-md">UPDATE NOW</button></div>
                </div>
                <div class="row">
                  <div class="col-md-12">(Please ensure all information is upto date for accurate results)</div>
                </div>
                <div class="row">
                  <div class="col-md-12">&nbsp;</div>
                </div>
              </div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row">
              <div class="col-sm-3" style="margin-bottom: -20px!important;">
                <div class="progress" style="height: 8px!important;">
                  <div class="progress-bar active" name="CVEprogress" role="progressbar" aria-valuenow="80"
                    aria-valuemin="0" aria-valuemax="100" style="width:100%">
                  </div>
                </div>
              </div>
              <div class="col-sm-6" style="margin-bottom: -20px!important;"></div>
            </div>
            <div class="row">
              <div class="col-sm-3"><span style="font-size: 10px!important;">Json file download is completed</span>
              </div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row">
              <div class="col-sm-3" style="margin-bottom: -20px!important;">
                <div class="progress" style="height: 8px!important;">
                  <div class="progress-bar active" name="CVEprogress" role="progressbar" aria-valuenow="80"
                    aria-valuemin="0" aria-valuemax="100" style="width:80%">
                  </div>
                </div>
              </div>
              <div class="col-sm-6" style="margin-bottom: -20px!important;"></div>
            </div>
            <div class="row">
              <div class="col-sm-3"><span style="font-size: 10px!important;">File insertion to database is in
                  progress</span></div>
            </div>
          </div>
          <div class="tab-pane fade" id="nav-profile" role="tabpanel" aria-labelledby="nav-profile-tab">

            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row">
              <div class="col-md-12">
                <h5>From this page , you can manage the available security content updates. You can synchronize the
                  security content, monitor the progress of sync operations, or can view the details of a security
                  content.</h5>
              </div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row" style="background-color:#d9edf7!important;">
              <div class="col-md-12">
                <h6><i class="fa-solid fa-circle-info text-primary" style="font-size:15px!important;"></i> New security
                  definitions are available.</h6>
              </div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row">
              <div class="col-md-8"></div>
              <div class="col-md-2"><button type="button" class="btn btn-outline-primary"
                  style="width: 155px!important;">VIEW DETAILS</button></div>
              <div class="col-md-2"><button type="button" class="btn btn-primary btn-md"
                  style="width: 155px!important;">RE-SYNC</button></div>
            </div>
            <div class="row">
              <div class="col-md-12">Status</div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row">
              <div class="col-md-6">
                <div class="dropdown">
                  <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton"
                    data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">All <i
                      class="bi bi-chevron-down"></i></button>
                  <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                    <a class="dropdown-item" href="#">Action</a>
                    <a class="dropdown-item" href="#">Another action</a>
                    <a class="dropdown-item" href="#">Something else here</a>
                  </div>
                </div>
              </div>
              <div class="col-md-offset-3 col-md-2">
                <form class="form-inline my-2 my-lg-0">
                  <input class="form-control mr-sm-2" type="search" placeholder="Search" value="&#128269; Search" search
                    aria-label="Search" style="width: 283px!important;">
                </form>
              </div>
            </div>
            <div class="row">
              <div class="col-md-12">&nbsp;</div>
            </div>
            <div class="row" style="box-shadow: 1px 1px 1px #3333333d!important;">
              <div class="col-md-12">
                <table class="table">
                  <thead>
                    <tr>
                      <th scope="col" style="text-align: left!important;">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault">Security Profile Name</label>
                        </div>
                      </th>
                      <th scope="col">Assessment Type</th>
                      <th scope="col">Platform</th>
                      <th scope="col">Last Updated</th>
                      <th scope="col">Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Non-Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:orange!important;">Sync Required</p>
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:Green!important;">Synced</p>
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Non-Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:Green!important;">Synced</p>
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:Green!important;">Synced</p>
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Non-Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:Green!important;">Synced</p>
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:Green!important;">Synced</p>
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Non-Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:Green!important;">Synced</p>
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:Green!important;">Synced</p>
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Non-Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:Green!important;">Synced</p>
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:Green!important;">Synced</p>
                      </td>
                    </tr>
                    <tr>
                      <th scope="row">
                        <div class="form-check">
                          <input class="form-check-input" type="checkbox" value="" id="CVEflexCheckDefault">
                          <label class="form-check-label" for="CVEflexCheckDefault"> Microsoft Windows 10 OvalPatch
                            Definitions</label>
                        </div>
                      </th>
                      <td>Configuration</td>
                      <td>Non-Windows</td>
                      <td>Fri Oct 14 07:54:59 UTC 2022</td>
                      <td>
                        <p style="color:Green!important;">Synced</p>
                      </td>
                    </tr>
                  </tbody>
                </table>

                <div class="row">
                  <div class="col-md-12">&nbsp;</div>
                </div>
                <div class="row">
                  <div class="col-md-offset-7 col-md-2">
                    <div class="dropdown">
                      Rows per page: 11
                      <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton"
                        data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                        <i class="bi bi-chevron-down"></i></button>
                      <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
                        <a class="dropdown-item" href="#">2</a>
                        <a class="dropdown-item" href="#">3</a>
                        <a class="dropdown-item" href="#">4</a>
                      </div>
                    </div>
                  </div>
                  <div class="col-md-offset-1 col-md-2">
                    1-5 of 13 <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton"
                      data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                      <i class="bi bi-chevron-left"></i></button>
                    <button class="btn btn-secondary dropdown-toggle" type="button" id="dropdownMenuButton"
                      data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                      <i class="bi bi-chevron-right"></i></button>
                  </div>
                </div>
              </div>
            </div>

          </div>
        </div>

    </section>
  </main>

</body>

</html>