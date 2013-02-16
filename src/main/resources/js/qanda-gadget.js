
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
                interval: gadget.getPref("issinterval")
            },
            success: function(data){
                gadget.hideLoading();
                if(data && data.length > 0){
                    console.log("rendering questions: success");
                    //reset the fields
                    for(var i = 0; i < data.length; i++) {
                        gadget.getView().append(formatQuestion(gadget, data[i]));
                    }
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
    
    function formatQuestion(gadget, q){
        var html = "";
        // public String issueKey;
        // public String issueSummary;
        // public String questionText;
        // public String status;
        // public boolean answered;\
        
        html += "<div class='qanda-panel-item qanda-question-panel qanda-gadget-panel'>";
        html += "<a href='" + contextPath + "/browse/" + q.issueKey + "?page=ro.agrade.jira.qanda:qanda-tabpanel'>" + q.issueKey + "</a> " + q.issueSummary;
        
        // TODO ## ============= HEADER
        // <span class='qandauser'>$uiFormatter.formatUser($question.user) $i18n.getText("qanda.panel.asked") </span>
        // <span class='qandadateq'>&nbsp;-&nbsp;$uiFormatter.formatTimeStamp($question.timeStamp)</span>
                     
        if(q.answered) {
            html += "<span class='aui-lozenge aui-lozenge aui-lozenge-complete'>" + gadget.getMsg("qanda.status.answered") +"</span>";
        } else if(q.status == "OPEN") {
            html += "<span class='aui-lozenge aui-lozenge-subtle aui-lozenge-error'>" + gadget.getMsg("qanda.status.noanswer") +"</span>";
        }
        html += "<div class='user-content-block'>"+ q.questionText +"</div>"
        html += "</div>";                     

        return html;
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