$(function () {

    // display form at start on validation failures (non-empty 'formAction' field
    // indicates that earlier submitted form needs to be amended and re-submitted)
    var action = $("#users-form input[name='formAction']").val();
    if (action !== "") {
        switch (action) {
            case "delete":
                switchFormToDelete();
                break;
            case "update":
                switchFormToUpdate();
                break;
        }
        $("#users-form").show();
    }

    // handle filters options change
    $(".filters select").on("change", function () {
        var filtersUrl = $("#users-form").prop("action").replace(/\/[^\/]*$/, "/filters?");
        $(".filters select").each(function () {
            filtersUrl += $(this).prop("name") + "=" + $(this).val() + "&";
        });
        window.location.href = filtersUrl;
    });

    // handle 'select-row' radio click
    $("#users-table input[name='select-row']").on("click", function () {
        var actionURL = $("#users-form").prop("action");
        if (/\/(delete|update)/.test(actionURL))
            showForm();
    });

    // handle 'delete-button' click
    $("#delete-button").on("click", function () {
        switchFormToDelete();
        showForm();
    });

    function switchFormToDelete() {
        $("#users-form").prop("action", $("#users-form").prop("action").replace(/\/[^\/]*$/, "/delete"));
        $("#users-form input[name='formAction']").val("delete");
        $("#users-form span.notice").text("Are you sure you want to delete this user?");
        $("#users-form input, #users-form select").prop("disabled", true);
    }

    // handle 'update-button' click
    $("#update-button").on("click", function () {
        switchFormToUpdate();
        showForm();
    });

    function switchFormToUpdate() {
        $("#users-form").prop("action", $("#users-form").prop("action").replace(/\/[^\/]*$/, "/update"));
        $("#users-form input[name='formAction']").val("update");
        $("#users-form span.notice").text("Edit user's info and submit changes.");
        $("#users-form input").prop("disabled", true);
        $("#users-form select").prop("disabled", false);
    }

    // handle form buttons clicks
    $("#users-form").on("submit", function () {
        $(this).hide();
        $(".loader-wrapper").show();
        $("#users-form input, #users-form select").prop("disabled", false);
    });

    $("#users-form button.cancel").on("click", function () {
        var usersUrl = $("#users-form").prop("action").replace(/\/[^\/]*$/, "");
        window.location.href = usersUrl;
    });

    // helper function for form auto-filling and showing
    function showForm() {
        var selectedRow = $("#users-table input[name='select-row']:checked").closest("tr");
        if (selectedRow.length === 0) {
            $("#users-form").hide();
            alert("No row selected!");
        } else {
            $("#users-form input[name='username']").val(selectedRow.find("td:nth-child(2)").text());
            $("#users-form input[name='firstName']").val(selectedRow.find("td:nth-child(3)").text());
            $("#users-form input[name='lastName']").val(selectedRow.find("td:nth-child(4)").text());
            $("#users-form input[name='email']").val(selectedRow.find("td:nth-child(5)").text());
            var authority = selectedRow.find("td:nth-child(6)").text();
            $("#users-form select[name = 'authority'] option[value=" + authority + "]").prop("selected", true);
            var enabled = selectedRow.find("td:nth-child(7)").text().toUpperCase();
            $("#users-form select[name = 'enabled'] option[value=" + enabled + "]").prop("selected", true);
            $("#users-form").fadeIn(250);
        }
    }

});
