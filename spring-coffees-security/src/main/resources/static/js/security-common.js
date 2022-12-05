$(function () {

    // password repeat check
    $("form").on("submit", function (e) {
        if ($("#password").val() !== $("#password-repeat").val()) {
            e.preventDefault();
            $("form span.password-repeat-error").slideDown(250);
        }
    });

    $("form input[type='password']").on("change", function () {
        if ($("#password").val() === $("#password-repeat").val()) {
            $("form span.password-repeat-error").slideUp(250);
        }
    });

});
