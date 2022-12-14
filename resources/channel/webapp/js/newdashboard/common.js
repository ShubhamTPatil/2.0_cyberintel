$(function () {

    var tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'))
    var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl)
    })

    $('input[type="checkbox"]').not('.selectAll').change(function (e) {
        let checkboxName = $(e.target).attr('name');
        let closestSelectAll = $(e.target).closest('table').find('.selectAll');
        let length = $('input[name=' + checkboxName + ']').length;
        let checkedLength = $('input[name=' + checkboxName + ']:checked').length;

        if (checkedLength != length) {
            closestSelectAll.prop('checked', false);
        } else {
            closestSelectAll.prop('checked', true);
        }
    });

    $(".selectAll").click(function (e) {
        let tableId = $(e.target).closest('table').attr('id');
        $('#' + tableId).find('input[type="checkbox"]').prop('checked', $(e.target).is(':checked'));
    });
});

function sidebarToggle() {
    $('body').toggleClass('toggle-sidebar');
};