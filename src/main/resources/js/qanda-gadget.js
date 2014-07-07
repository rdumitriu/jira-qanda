//
// Copyright (c) AGRADE Software. Please read src/main/resources/META-INF/LICENSE
// or online document at: https://github.com/rdumitriu/jira-qanda/wiki/LICENSE
//

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


    function getTemplate(gadget, args, baseUrl, qURL) {
        // reset view
        gadget.getView().empty();
        gadget.getView().append("<div class='qanda-gadget-view'></div>");
        var content = gadget.getView().find(".qanda-gadget-view");
        
        if(gadget.getPref("project") == "") {
             var html = "<br/><div class='aui-message aui-message-info'>"+ gadget.getMsg("qanda.gadget.not.configured")+"</div>";
             content.append(html);
             return;
        }

        gadget.showLoading();
        AJS.$.ajax({
            type: 'POST',
            url: baseUrl + qURL,
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
                        content.append(formatQuestion(gadget, data[i], baseUrl));
                    }
                } else {
                    var html = "<br/><div class='aui-message aui-message-info'>"+gadget.getMsg("qanda.gadget.no.questions")+"</div>";
                    content.append(html);
                }
                gadget.resize();
            },
            error: function() {
                gadget.hideLoading();
                var html = "<br/><div class='aui-message aui-message-error'>" + gadget.getMsg("qanda.gadget.generic.error") + "</div>";
                content.append(html);
                gadget.resize();
            }
        });

    }
    
    function formatQuestion(gadget, q, baseUrl){
        var html = "";
        
        html += "<div class='qanda-panel-item qanda-gadget-panel'>";
        html += "<span class='qandauser'>" + q.user + "&nbsp;" + gadget.getMsg("qanda.panel.asked.for.issue") + "&nbsp;</span>";
        html += "<a class='issue-link' href='" + baseUrl + "/browse/" + q.issueKey + "?page=ro.agrade.jira.qanda-pro:qanda-tabpanel'>" + q.issueKey + " - " + q.issueSummary + "</a>";
                     
        if(q.answered) {
        	html += "<span class='aui-lozenge aui-lozenge aui-lozenge-complete'>" + q.noAnswers + "&nbsp;";
        	if(q.noAnswers == 1) {
        		html += gadget.getMsg("qanda.status.answers.singular"); 
        	} else {
        		html += gadget.getMsg("qanda.status.answers.plural");
        	}
        	html += "</span>";        		
        	
        } else if(q.status == "OPEN") {
            html += "<span class='aui-lozenge aui-lozenge-subtle aui-lozenge-error'>" + gadget.getMsg("qanda.status.noanswer") +"</span>";
        }
        html += "<div class='user-content-block'>"+ q.questionText +"</div>"
        html += "<div class='qandadateq'>"+ q.timestamp +"</span>";
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
                    label: "Chose the project or filter",
                    description:"Choose the project or filter from which you want to see questions",
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
                }
            ]
        };
    }

    return {
        template: getTemplate,
        descriptor: getDescriptor
    };
})();
