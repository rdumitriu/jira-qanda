
//::TODO:: better checks on texts

var QANDA = (function () {

    function createAskPanelContent() {
        var html = '<form class="aui">';
            html += '<div class="field-group">';
                html += '<label for="qandaquestiontext">Your question:</label>';
                html += '<textarea cols="30" rows="4" class="textarea" type="text" id="qandaquestiontext" name="qandaquestiontext" title="Question"></textarea>';
            html += '</div>';
        html += '</form>';
        return html;
    }

    function askQuestion(base, issueKey) {
        console.log("adding question");

        var dialog = new AJS.Dialog({
            width:500,
            height:200,
            id:"quanda-addquestion",
            closeOnOutsideClick: false
        });
        dialog.addHeader("Add Question");
        dialog.addPanel("Panel1", createAskPanelContent(), "panel-body");
        dialog.get("panel:0").setPadding(10);

        dialog.addSubmit("Ask your question ...", function() {
            var txt = AJS.$('#qandaquestiontext').val();
            if(txt.length > 0) {
                console.log("saving question on " + issueKey + " base: " + base);
                saveNewQuestion(base, issueKey);
                dialog.hide();
                dialog.remove();
            }
        });

        dialog.addCancel("Forget it", function() {
            dialog.hide();
            dialog.remove();
        });

        dialog.show();
        dialog.updateHeight();
    }

    function reloadWindow() {
        AJS.dim();
        window.location.reload();
    }

    function saveNewQuestion(base, issueKey) {
        AJS.$.ajax ({
            type: 'POST',
        	url: base + "/rest/agrade/qanda/latest/panel/addquestion",
        	data: {
        	    issueKey : issueKey,
        	    question : AJS.$('#qandaquestiontext').val(),
        	},
        	success: function(data){
        	    if(data){
        	        console.log("added question: success");
        	        //reset the fields
        	    }
        	    reloadWindow();
        	},
        	error: function() {
        	    AJS.$('#quanda-error').empty();
        	    AJS.messages.error("#quanda-error", {
        	        title:"There was an error saving your question ...",
                    body: "<p>Please check the log for details or report the problem to the developer</p>"
                });
                AJS.$('#quanda-error').removeClass('hidden');
        	}
        });
    }

    function createRespondPanelContent() {
        var html = '<form class="aui">';
            html += '<div class="field-group">';
                html += '<label for="qandaanswertext">Your answer:</label>';
                html += '<textarea cols="30" rows="4" class="textarea" type="text" id="qandaanswertext" name="qandaanswertext" title="Answer"></textarea>';
            html += '</div>';
        html += '</form>';
        return html;
    }

    function respondToQuestion(base, qid) {
        console.log("answering question: " + qid);

        var dialog = new AJS.Dialog({
            width:500,
            height:200,
            id:"quanda-addanswer",
            closeOnOutsideClick: false
        });
        dialog.addHeader("Respond");
        dialog.addPanel("Panel1", createRespondPanelContent(), "panel-body");
        dialog.get("panel:0").setPadding(10);

        dialog.addSubmit("Respond ...", function() {
            var txt = AJS.$('#qandaanswertext').val();
            if(txt.length > 0) {
                console.log("saving answer on question " + qid + " base: " + base);
                saveNewAnswer(base, qid);
                dialog.hide();
                dialog.remove();
            }
        });

        dialog.addCancel("Forget it", function() {
            dialog.hide();
            dialog.remove();
        });

        dialog.show();
        dialog.updateHeight();
    }

    function saveNewAnswer(base, qid) {
        AJS.$.ajax ({
            type: 'POST',
            url: base + "/rest/agrade/qanda/latest/panel/addanswer",
            data: {
                questionId : qid,
                answer : AJS.$('#qandaanswertext').val(),
            },
            success: function(data){
                if(data){
                    console.log("added answer: success");
                    //reset the fields
                }
                reloadWindow();
            },
            error: function() {
                AJS.$('#quanda-error').empty();
                AJS.messages.error("#quanda-error", {
                    title:"There was an error saving your answer ...",
                    body: "<p>Please check the log for details or report the problem to the developer</p>"
                });
                AJS.$('#quanda-error').removeClass('hidden');
            }
        });
    }

    function deleteQuestion(base, qid) {
        console.log("delete question: " + qid);

        AJS.$.ajax ({
            type: 'POST',
            url: base + "/rest/agrade/qanda/latest/panel/deletequestion",
            data: {
                questionId : qid,
            },
            success: function(data){
                if(data){
                    console.log("deleted question: success");
                }
                reloadWindow();
            }
        });
    }

    function toggleAnswerFlag(base, aid, flag) {
        AJS.$.ajax ({
            type: 'POST',
            url: base + "/rest/agrade/qanda/latest/panel/setapproval",
            data: {
                answerId : aid,
                approval: flag
            },
            success: function(data){
                if(data){
                    console.log("changed answer flag: success");
                }
                reloadWindow();
            }
        });
    }

    function approveAnswer(base, aid) {
        console.log("approve answer: " + aid);
        toggleAnswerFlag(base, aid, "true");
    }

    function clearApprovalAnswer(base, aid) {
        console.log("disapprove answer: " + aid);
        toggleAnswerFlag(base, aid, "false");
    }

    function deleteAnswer(base, aid) {
        console.log("delete answer: " + aid);
        AJS.$.ajax ({
            type: 'POST',
            url: base + "/rest/agrade/qanda/latest/panel/deleteanswer",
            data: {
                answerId : aid,
            },
            success: function(data){
                if(data){
                    console.log("delete answer: success");
                }
                reloadWindow();
            }
        });
    }

    return {
        askQuestion: askQuestion,
        deleteQuestion: deleteQuestion,
        respondToQuestion: respondToQuestion,
        approveAnswer: approveAnswer,
        clearApprovalAnswer: clearApprovalAnswer,
        deleteAnswer : deleteAnswer
    }
})();

AJS.$(document).ready(function() {
    // the add button for questions
    AJS.$('#quanda_addquestion').bind("click", function(e) {
        QANDA.askQuestion(AJS.$(this).attr('baseUrl'), AJS.$(this).attr('issueKey'));
    });

    AJS.$('a[id^="quanda_delquestion_"]').bind("click", function(e) {
        QANDA.deleteQuestion(AJS.$(this).attr('baseUrl'), AJS.$(this).attr('questionId'));
    });


    AJS.$('a[id^="quanda_addanswer_"]').bind("click", function(e) {
        QANDA.respondToQuestion(AJS.$(this).attr('baseUrl'), AJS.$(this).attr('questionId'));
    });

    AJS.$('a[id^="quanda_approvelink_"]').bind("click", function(e) {
        QANDA.approveAnswer(AJS.$(this).attr('baseUrl'), AJS.$(this).attr('answerId'));
    });

    AJS.$('a[id^="quanda_disapprovelink_"]').bind("click", function(e) {
        QANDA.clearApprovalAnswer(AJS.$(this).attr('baseUrl'), AJS.$(this).attr('answerId'));
    });

    AJS.$('a[id^="quanda_deleteanswer_"]').bind("click", function(e) {
        QANDA.deleteAnswer(AJS.$(this).attr('baseUrl'), AJS.$(this).attr('answerId'));
    });
});