$(function () {

    // display form at start on validation failures (non-empty 'formAction' field
    // indicates that earlier submitted form needs to be amended and re-submitted)
    var action = $("#suppliers-form input[name='formAction']").val();
    if (action !== "") {
        switch(action) {
            case "delete": switchFormToDelete(); break;
            case "update": switchFormToUpdate(); break;
            case "save": switchFormToSave(); break;
        }
        $("#suppliers-form").show();
    }

    // handle 'order-by-name' checkbox change
    $("#order-by-name-filter").on("change", function () {
        var filtersUrl = $("#suppliers-form").prop("action").replace(/\/[^\/]*$/, "/filters?");
        filtersUrl += "orderByName=" + $(this).prop("checked");
        window.location.href = filtersUrl;
    });

    // handle 'select-row' radio click
    $("#suppliers-table input[name='select-row']").on("click", function () {
        var actionURL = $("#suppliers-form").prop("action");
        if (/\/(delete|update)/.test(actionURL))
            showForm();
    });

    // handle 'delete-button' click
    $("#delete-button").on("click", function () {
        switchFormToDelete();
        showForm();
    });

    function switchFormToDelete() {
        $("#suppliers-form").prop("action", $("#suppliers-form").prop("action").replace(/\/[^\/]*$/, "/delete"));
        $("#suppliers-form input[name='formAction']").val("delete");
        $("#suppliers-form span.notice").text("Are you sure you want to delete this supplier?");
        $("#suppliers-form input").prop("disabled", true);
    }

    // handle 'update-button' click
    $("#update-button").on("click", function () {
        switchFormToUpdate();
        showForm();
    });

    function switchFormToUpdate() {
        $("#suppliers-form").prop("action", $("#suppliers-form").prop("action").replace(/\/[^\/]*$/, "/update"));
        $("#suppliers-form input[name='formAction']").val("update");
        $("#suppliers-form span.notice").text("Edit supplier's info and submit changes.");
        $("#suppliers-form input").prop("disabled", false);
    }

    // handle 'add-new-button' click
    $("#add-new-button").on("click", function () {
        switchFormToSave();
        $("#suppliers-form input[name='id']").val(0);
        $("#suppliers-form input[type='text']").val("");
        $("#suppliers-form").fadeIn(250);
    });

    function switchFormToSave() {
        $("#suppliers-form").prop("action", $("#suppliers-form").prop("action").replace(/\/[^\/]*$/, "/save"));
        $("#suppliers-form input[name='formAction']").val("save");
        $("#suppliers-form span.notice").text("New supplier will be added to the database.");
        $("#suppliers-form input").prop("disabled", false);
    }

    // handle form buttons clicks
    $("#suppliers-form").on("submit", function () {
        $(this).hide();
        $(".loader-wrapper").show();
        $("#suppliers-form input").prop("disabled", false);
    });

    $("#suppliers-form button.cancel").on("click", function () {
        var usersUrl = $("#suppliers-form").prop("action").replace(/\/[^\/]*$/, "");
        window.location.href = usersUrl;
    });

    // helper function for form auto-filling and showing
    function showForm() {
        var selectedRow = $("#suppliers-table input[name='select-row']:checked").closest("tr");
        if (selectedRow.length === 0) {
            $("#suppliers-form").hide();
            alert("No row selected!");
        } else {
            $("#suppliers-form input[name='id']").val(selectedRow.attr("data-supplier-id"));
            $("#suppliers-form input[name='name']").val(selectedRow.find("td:nth-child(2)").text());
            $("#suppliers-form input[name='email']").val(selectedRow.find("td:nth-child(3)").text());
            $("#suppliers-form input[name='address']").val(selectedRow.find("td:nth-child(4)").text());
            $("#suppliers-form").fadeIn(250);
        }
    }

});
