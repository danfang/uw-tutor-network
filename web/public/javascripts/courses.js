$(document).ready(function() {

    var loc = location.pathname.split("/");
    var major = loc[loc.length - 1];
    var school = loc[loc.length - 2];
    tutorData = {};
    var currentCourse = null;
    var coursesLoaded = false;
    var tutorsLoaded = false;

    $.get("/api/" + school + "/" + major + "/courses")
     .done(function(data) {
        courses = data.courses;
        tutoring = data.tutoring;

        for(var index in courses) {
            var course = courses[index];
            var html = $('<div class="course list-group-item">');
            html.attr("id", course.id);
            var nameHtml = $('<div class="name">').html(course.name);
            if (tutoring && tutoring.some(function(el) { return el.course === course.id })) {
                nameHtml.prepend($('<i class="fa fa-graduation-cap">'));
            }
            html.append(nameHtml);
            $('#course-content .list-group').append(html);
        }
        coursesLoaded = true;
        if (tutorsLoaded) {
            startUI();
        }
     })
    .fail(function() {
        console.log("Failed to get courses.");
    });
    refreshTutors();

function refreshTutors() {
    $.get("/api/" + school + "/" + major + "/tutors")
     .done(function(data) {
        tutorData = data;
        $("#tutor-content").html("");
        for (var user in tutorData) {
            $userDiv = $('<div class="all-tutorProfile">');
            $userEmail = $('<div class="tutorEmail">').html(user);
            $userEmail.prepend($('<span class="title">').html("Email: "));
            $userDiv.append($userEmail);
            $userDiv.append($('<h4>').html('Courses Tutoring'));
            $courseUl = $('<ul class="coursesTutored">');
            for (var item in tutorData[user]) {
                $courseUl.append($('<div class="courseTitle">').html(tutorData[user][item].course));
            }
            $userDiv.append($courseUl);
            $("#tutor-content").append($userDiv);
        }
        tutorsLoaded = true;
        reloadInfoPanelTutors();
        if (coursesLoaded) {
            startUI();
        }
     })
    .fail(function() {
        console.log("Failed to get tutors.");
    });
}

function reloadInfoPanel(el, course) {
    $('.course.selected').removeClass("selected");
    $(el).addClass("selected");
    $("#info-pane .title").attr("href", course.link);
    $("#info-pane .title").html(course.name);
    var info = $('<div>');
    if (course.offered) info.append($('<span class="tag offered">').html(course.offered));
    if (course.prereqs) info.append($('<span class="tag prereqs">').html(course.prereqs));
    if (course.desc) info.append($('<p class="desc">').html(course.desc));
    $("#info-pane #info").html(info.html())
    $("#info-pane").show();
    $("#nav-tools").show();
}

function reloadInfoPanelTutors() {
    if (currentCourse) {
        var curId = currentCourse.id;
        $("#tutors").html("No tutors available.");
        var $tutorInfo = $('<div class="tutorInfo">');
        var hasTutors = false;
        for (var user in tutorData) {
            for (var item in tutorData[user]) {
                if (tutorData[user][item].course === curId) {
                    hasTutors = true;
                    $tutorInfo.append($('<div class="tutorProfile">').html(user));
                }
            }
        }
        if (hasTutors) $("#tutors").html($tutorInfo.html());
    }
}

function startUI() {
    $('#showAllCourses').click(function() {
        $(".sidebar-left .current").removeClass("current");
        $(this).addClass("current");
        $('#course-content').show();
        $('#tutor-content').hide();
    });

    $('#showAllTutors').click(function() {
        $(".sidebar-left .current").removeClass("current");
        $(this).addClass("current");
        $('#course-content').hide();
        $('#tutor-content').show();
    });

    $('.course').each(function(index) {
        $(this).click(function() {
            var course = courses[index];
            currentCourse = course;

            var tutorDiv = $("#tutor-start");
            if (tutoring && tutoring.some(function(el) { return el.course === course.id })) {
                tutorDiv.html("Unregister").removeClass("register");
            } else {
                tutorDiv.html("Become a tutor").addClass("register");
            }

            reloadInfoPanel(this, course);
            reloadInfoPanelTutors();
        })
    });

    var $info = $("#switch-info");
    var $tutors = $("#switch-tutors");

    $info.click(function() {
        $("#tutors").hide();
        $("#info").show();
        $tutors.removeClass("selected");
        $info.addClass("selected");
    })

    $tutors.click(function() {
        $("#info").hide();
        $("#tutors").show();
        $info.removeClass("selected");
        $tutors.addClass("selected");
    })

    $("#tutor-start").click(function() {
        if (currentCourse != null) {
            var toDelete = false;
            if (tutoring.some(function(el) { return el.course === currentCourse.id })) {
                toDelete = true;
            }
            $.ajax({
                type: "POST",
                url: "/api/tutor",
                data: JSON.stringify({
                    "school": school,
                    "major": major,
                    "course": currentCourse.id,
                    "delete": toDelete
                }),
                contentType: "application/json"
             })
             .done(function() {
                if(!toDelete) {
                    tutoring.push({
                        "school": school,
                        "major": major,
                        "course": currentCourse.id,
                    });
                    $("#tutor-start").html("Unregister").removeClass("register");
                    $("#" + currentCourse.id + " .name").prepend($('<i class="fa fa-graduation-cap">'));
                    console.log("Now tutoring " + currentCourse.id)
                } else {
                    $("#tutor-start").html("Become a tutor").addClass("register");
                    tutoring = tutoring.filter(function(el) { return el.course !== currentCourse.id });
                    $("#" + currentCourse.id + " .fa").remove();
                    console.log("Unregistered from " + currentCourse.id);
                }
                refreshTutors();
             })
            .fail(function() {
                console.log("Failure to register/unregister.");
            });
        }
    });
}
});