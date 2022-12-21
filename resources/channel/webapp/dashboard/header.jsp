<!DOCTYPE html>
<html lang="en">

<header id="header" class="header fixed-top d-flex align-items-center">
    <div class="d-flex align-items-center justify-content-between">
      <a href="/shell/dashboard.do" class="logo d-flex align-items-center">
        <img
          src="/spm/images/harman_defensight_logo_h.png"
          style="min-height: 60px;" alt="Harman DefenSight">
      </a>
      <i onclick="sidebarToggle()" class="fa-solid fa-bars toggle-sidebar-btn"></i>
    </div>
    <nav class="header-nav ms-auto">
      <ul class="d-flex align-items-center">
        <li class="nav-item dropdown pe-3">
          <a class="nav-link nav-profile d-flex align-items-center pe-0" href="#" data-bs-toggle="dropdown">
            <i class="fa-regular fa-user rounded-circle"></i>
            <!-- <img src="assets/img/profile-img.jpg" alt="Profile" class="rounded-circle">  -->
            <span class="d-none d-md-block dropdown-toggle ps-2">Admin</span> </a>
          <ul class="dropdown-menu dropdown-menu-end dropdown-menu-arrow profile">
            <li> <a class="dropdown-item d-flex align-items-center"> <i class="fa-solid fa-user-pen"></i> <span>
                  Status</span> </a></li>
            <li>
              <hr class="dropdown-divider">
            </li>
            <li> <a class="dropdown-item d-flex align-items-center"> <i class="fa-regular fa-address-card"></i> <span>
                  About</span> </a></li>
            <li>
              <hr class="dropdown-divider">
            </li>
            <li> <a class="dropdown-item d-flex align-items-center"> <i
                  class="fa-solid fa-arrow-right-from-bracket"></i> <span>Log
                  Out</span> </a></li>
          </ul>
        </li>
      </ul>
    </nav>
  </header>
  </html>