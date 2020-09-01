$(function () {

    // handle pagination events
    $('.pagination').jqPagination({
        paged: function (pageNum) {
            var salesUrl = new URL(location.href);
            salesUrl.searchParams.set("page", pageNum)
            location.href = salesUrl.toString();
        }
    });

    // initialize calendar date pickers
    $("#start-date-filter").datepicker({"dateFormat": "dd.mm.yy"})
        .datepicker("setDate", $("#start-date-filter").attr("value"));
    $("#end-date-filter").datepicker({"dateFormat": "dd.mm.yy"})
        .datepicker("setDate", $("#end-date-filter").attr("value"));

    // handle filters change events
    $("div.filters *[id$='-filter']").on("change", function () {
        var filtersUrl = $("#sales-form").prop("action").replace(/\/[^\/]*$/, "/filters?");
        $("div.filters *[id$='-filter']").each(function () {
            filtersUrl += $(this).prop("name") + "=" + $(this).val() + "&";
        });
        window.location.href = filtersUrl;
    });

    // handle 'date-time-order-clicker'
    $("#date-time-order-clicker").on("click", function () {
        var direction = ($(this).attr("data-direction") === "ASC") ? "desc" : "asc";
        var salesUrl = new URL(location.href);
        salesUrl.searchParams.set("sort", "dateTime," + direction)
        location.href = salesUrl.toString();
    });

    // handle 'delete-button' click
    $("#delete-button").on("click", function () {
        $("#sales-form").fadeIn(250);
    });

    // handle form submit event
    $("#sales-form").on("submit", function (event) {
        var selectedSales = $("#sales-table input[name ='selectedSales']:checked");
        if (selectedSales.length === 0) {
            event.preventDefault();
            alert("No row selected!");
        } else {
            $(this).hide();
            $(".loader-wrapper").show();
            $(this).submit();
        }
    });

});
