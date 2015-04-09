$(document).ready(function() {

    var loc = location.pathname.split("/");
    major = loc[loc.length - 1];
    school = loc[loc.length - 2];
    currentCourse = null;
    currentIndex = -1;
    indexTutors = [];

    $.get("/api/" + school + "/" + major)
     .done(function(data) {
        courses = data.courses;
        tutoring = data.tutoring;

        for(var index in courses) {
            var course = courses[index];
            var html = $('<div class="course list-group-item">');
            var nameHtml = $('<div class="name">').html(course.name);
            for (var tutorIndex in tutoring) {
                if (tutoring[tutorIndex].course === course.id) {
                    indexTutors.push(Number(index));
                    nameHtml.prepend($('<i class="fa fa-graduation-cap">'));
                }
            }
            html.append(nameHtml)
            $('#course-content .list-group').append(html);
        }

        startUI(school, major);
     })
    .fail(function() {
        console.log("Fail");
    });

});

function startUI(school, major) {
    $('.course').each(function(index) {
        $(this).click(function() {
            var course = courses[index];
            currentCourse = course;
            currentIndex = index;
            $('.course.selected').removeClass("selected");
            $(this).addClass("selected");
            $("#info-pane .title").attr("href", course.link);
            $("#info-pane .title").html(course.name);
            var info = $('<div>');
            if (course.offered) {
                info.append($('<span class="tag offered">').html(course.offered));
            }
            if (course.prereqs) {
                info.append($('<span class="tag prereqs">').html(course.prereqs));
            }
            if (course.desc) {
                info.append($('<p class="desc">').html(course.desc));
            }
            $("#info-pane #info").html(info.html())
            $("#info-pane").show();
            $("#nav-tools").show();
        })
    });

    var $info = $("#switch-info");
    var $tutors = $("#switch-tutors");

    $info.click(function() {
        $tutors.removeClass("selected");
        $info.addClass("selected");
        $("#info").show();
    })

    $tutors.click(function() {
        $("#info").hide();
        $info.removeClass("selected");
        $tutors.addClass("selected");
    })

    $("#tutor-start").click(function() {
        if (currentCourse != null) {
            $("#tutor-start").html($("#tutor-start").html() == "Tutor" ? "Untutor": "Tutor");
            var toDelete = indexTutors.indexOf(currentIndex) != -1 ? true: false;
            $.ajax({
                type: "POST",
                url: "/tutor",
                data: JSON.stringify({
                    "school": school,
                    "major": major,
                    "course": currentCourse.id,
                    "delete": toDelete
                }),
                contentType: "application/json"
             })
             .done(function() {
                if(toDelete) {
                    $($('.course i')[currentIndex]).remove();
                } else {
                    $($('.course .name')[currentIndex]).prepend($('<i class="fa fa-graduation-cap">'));
                }
                console.log("Success!")
             })
            .fail(function() {
                console.log("Fail");
            });
        }
    });
}