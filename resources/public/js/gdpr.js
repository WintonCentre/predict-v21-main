// Tracking code wrapped

// This replaces previous scripts like ' async src="https://www.google-analytics.com/analytics.js" '
function loadScript( url, callback) {
    var script = document.createElement( "script" )
    script.type = "text/javascript";
    if(script.readyState) {  // only required for IE <9
        script.onreadystatechange = function() {
            if ( script.readyState === "loaded" || script.readyState === "complete" ) {
                script.onreadystatechange = null;
                callback();
            }
        };
    } else {  //Others
        script.onload = function() {
            callback();
        };
    }

    script.src = url;

    document.getElementsByTagName( "head" )[0].appendChild( script );
}

// Contains unique settings for google analytics
function initializeGAVars() {
    window.ga=window.ga||function(){(ga.q=ga.q||[]).push(arguments)};ga.l=+new Date;
    ga('create', "UA-1462795-22", 'auto');

    // Replace the following lines with the plugins you want to use.
    ga('require', 'urlChangeTracker');

    // ga('require', 'eventTracker');
    ga('require', 'eventTracker', {
        attributePrefix: 'data-'
    });

    ga('send', 'pageview');

    // For GDPR
    ga('set', 'anonymizeIp', true);

    // console.log('initializeGAVars complete')
}

function startHotJar() {
    (function(h,o,t,j,a,r){
        h.hj=h.hj||function(){(h.hj.q=h.hj.q||[]).push(arguments)};
        h._hjSettings={hjid:879744,hjsv:6};
        a=o.getElementsByTagName('head')[0];
        r=o.createElement('script');r.async=1;
        r.src=t+h._hjSettings.hjid+j+h._hjSettings.hjsv;
        a.appendChild(r);
    })(window,document,'https://static.hotjar.com/c/hotjar-','.js?sv=');
}

// Main function to Enable GA. Loads in two GA script and runs our settings.
function startGA() {
    loadScript('https://www.google-analytics.com/analytics.js', function() {
        // console.log('Loaded script => https://www.google-analytics.com/analytics.js')
        initializeGAVars()
    });

    loadScript('/js/autotrack.js', function() {
        // console.log('Loaded script => autotrack.js')
    });
}

// Uncommenting below will enable
// startHotJar();
// startGA();

var allow_cookie = undefined;

// GDPR Code.

function btnAllowOnClick() {
    allow_cookie = true;
}

function btnNotAllowOnClick() {
    allow_cookie = false;
}


function gdprBtnOnClick(e) {
    e.preventDefault();
    // console.log(allow_cookie);
    if (allow_cookie == true) {
        localStorage.setItem('user-analytics', 'true');
    }
    localStorage.setItem('user-tc', 'true');
    initialiseGDPRState();
    return false;
}

function initialiseGDPRState() {
    var userAnalytics = localStorage.getItem('user-analytics');
    var userTc = localStorage.getItem('user-tc');
    // console.log(userTc);

    // Initial value
    if (userTc == 'false' || userTc == null || userTc == undefined) {
        var gdprContainer = document.getElementsByClassName('gdpr-container')[0];
        gdprContainer.style.display = 'block';
    }

    // Case where user agrees
    if (userAnalytics == 'true' && userTc == 'true') {
        var gdprContainer = document.getElementsByClassName('gdpr-container')[0];
        gdprContainer.style.display = 'none';
        startHotJar();
        startGA();
    } else if (userTc == 'true') {
        var gdprContainer = document.getElementsByClassName('gdpr-container')[0];
        gdprContainer.style.display = 'none';
    }
}

function insertGDPRHtml() {
    // Line Wrap for IE9
    // $('#gdprContainer').html("            <form onsubmit=\"gdprBtnOnClick(event)\">\n                <div class=\"row\">\n                    <div class=\"col-lg-7 col-md-7 col-sm-7 col-xs-12 vcenter\">We use google\n                        analytics and hotjar improve our website. For more information, visit our\n                        <a href=\"https://breast.predict.nhs.uk/legal/privacy\" class=\"\">Privacy Policy.</a>\n                    </div><!--\n                    --><div class=\"col-lg-3 col-md-3 col-sm-3 col-xs-6 vcenter col-centered \">\n                            <label class=\"gdpr-checkbox-inline\"><input type=\"checkbox\" style=\"margin-right: 5px;\" name=\"analytics\" value=\"analytics\" id=\"analyticsCheckBox\">Analytics</label>\n                            <label class=\"gdpr-checkbox-inline\" id=\"tcLabel\"><input type=\"checkbox\" style=\"margin-right: 5px;\" name=\"tc\" value=\"tc\" id=\"tcCheckbox\" required>Terms and Conditions</label>\n                    </div><!--\n                    --><div class=\"col-lg-2 col-md-2 col-sm-2 col-xs-6 vcenter col-centered text-right\">\n                        <a href=\"https://breast.predict.nhs.uk/legal/privacy\" class=\"btn btn-link btn-sm\" style=\"text-shadow: none\">See details</a>\n        <!--                <button class=\"btn btn-primary btn-sm\" style=\"width: 60px;\" onclick=\"gdprBtnOnClick()\">Ok</button>-->\n                        <input type=\"submit\" class=\"btn btn-primary btn-sm\" style=\"width: 60px;\" value=\"Ok\">\n                    </div><!--\n                    -->\n                </div>\n            </form>");

    var gdprContainer = document.getElementById("gdprContainer");
    gdprContainer.innerHTML = '<form onsubmit=gdprBtnOnClick(event)><div class=row><div class="gdpr-style col-lg-8 col-md-8 col-sm-8 col-xs-12 vcenter">May we use cookies to improve our website? For more information, visit our <a class=gdpr-style href=https://breast.predict.nhs.uk/legal/privacy>Privacy Policy.</a></div><div class="col-xs-12 vcenter col-centered col-lg-4 col-md-4 col-sm-4 text-right"><input class="gdpr-style btn btn-sm btn-default"id=submit-btn-not-allow onclick=btnNotAllowOnClick() style=margin-left:5px type=submit value="Don\'t Allow"> <input class="gdpr-style btn btn-sm btn-primary"id=submit-btn-allow onclick=btnAllowOnClick() style=margin-left:5px type=submit value=Allow></div></div></form>'
}
