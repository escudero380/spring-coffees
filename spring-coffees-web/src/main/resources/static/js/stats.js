$(function () {

    // handle 'limit' and 'months' filters change
    $("#limit-filter, #months-filter").on("change", function () {
        var url = new URL(location.href);
        url.searchParams.set($(this).prop("name"), $(this).val())
        location.href = url.toString();
    });

    // handle 'top-seller' filter change
    $("#top-seller-filter").on("change", function () {
        var url = new URL(location.href);
        url.pathname = url.pathname.replace(/(\/)[^\/]*$/, "$1" + $(this).val());
        location.href = url.toString();
    });

});
