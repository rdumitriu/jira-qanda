
var QANDAGADGET = (function() {

    function getProjects(baseUrl) {
        var ret;
        AJS.$.ajax({
            type: 'GET',
            url: baseUrl + "/rest/agrade/qanda/latest/gadget/projects",
            async: false,
            data: {
            },
            success: function(data){
                ret = data;
            }
        });
        return ret;
    }

    function getIssueInterval(baseUrl) {
        var ret;
        AJS.$.ajax({
            type: 'GET',
            url: baseUrl + "/rest/agrade/qanda/latest/gadget/intervals",
            async: false,
            data: {
            },
            success: function(data){
                ret = data;
            }
        });
        return ret;
    }


    function getTemplate(gadget, args, baseUrl) {
        // reset view
        gadget.getView().empty();

        if(gadget.getPref("project") == "") {
             var html = "<div class='aui-message aui-message-info'>Please configure me first!</div>";
             gadget.getView().append(html);
             return;
        }

        gadget.showLoading();
        AJS.$.ajax({
            type: 'POST',
            url: baseUrl + "/rest/agrade/qanda/latest/gadget/getquestions",
            data: {
                project: gadget.getPref("project"),
                interval: gadget.getPref("issinterval"),
            },
            success: function(data){
                gadget.hideLoading();
                if(data && data.length > 0){
                    console.log("rendering questions: success");
                    //reset the fields
                    var html = "<table class='aui' border='0'>";
                    for(var i = 0; i < data.length; i++) {
                        html += "<tr>";
                        html += "<td><a href='" + baseUrl + "/browse/" + data[i].issueKey + "?page=ro.agrade.jira.qanda:qanda-tabpanel'>" + data[i].issueKey + "" + data[i].issueSummary + "</a>";
                        html += "<div style='margin-left: 10px;'>";
                        if(!data[i].answered) {
                            html += "<span class='aui-lozenge aui-lozenge-subtle aui-lozenge-default'>No answer</span>";
                        } else if(!data[i].closed) {
                            html += "<span class='aui-lozenge aui-lozenge-subtle aui-lozenge-complete'>Answered</span>";
                        }
                        html += "</div>";
                        html += "</td>";
                        html += "<td>" + data[i].questionText + "</td>";
                        html += "</tr>";
                    }
                    html += "</table>";
                    gadget.getView().append(html);
                } else {
                    var html = "<div class='aui-message aui-message-info'>No questions asked, everything seems to be cristal clear</div>";
                    gadget.getView().append(html);
                }
                gadget.resize();
            },
            error: function() {
                gadget.hideLoading();
                var html = "<div class='aui-message aui-message-error'> Error! Please check the log for details ...</div>";
                gadget.getView().append(html);
                gadget.resize();
            }
        });

    }


    function getDescriptor(gadget, args, baseUrl) {
        return  {
            action: baseUrl + "/rest/agrade/qanda/latest/gadget/validate",
            theme : "gdt",
            fields: [
                {
                    id: "project-field",
                    class: "aui",
                    userpref: "project",
                    label: "Chose the project",
                    description:"Choose the project from which you want to see questions",
                    type: "select",
                    selected: gadget.getPref("project"),
                    options: getProjects(baseUrl)
                },
                {
                    id: "interval-field",
                    class: "aui",
                    userpref: "issinterval",
                    label: "Looking back interval",
                    description:"Choose the interval to examine issues with questions",
                    type: "select",
                    selected: gadget.getPref("issinterval"),
                    options: getIssueInterval(baseUrl)
                },
            ]
        };
    }

    return {
        template: getTemplate,
        descriptor: getDescriptor
    };
})();