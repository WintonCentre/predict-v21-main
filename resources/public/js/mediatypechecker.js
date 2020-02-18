// mediatypechecker.js
// CSS media types checking/detection script ver. 15-apr-2009 by Marcin Wiazowski (marcinwiazowski AT poczta DOT onet DOT pl)
// http://cssmedia.pemor.pl/
//
// You may freely use this script for any purposes (incl. commercial) or distribute it if only you want.
//
// Usage examples:
//   IsMediaType('screen')        - check if the current CSS medium is 'screen'
//   IsMediaType('screen, print') - check if the current CSS medium is 'screen' or 'print'
//
//   This script does NOT disable the Opera's Small-Screen Rendering technology when asking about
//   the 'handheld' media type in the Opera's mobile browsers.
//
// Return values:
//   -1 - error (browser too old, IE having 30 or more style sheets)
//   0  - tested media type(s) not active
//   1  - tested media type(s) active
//
//
// Tested both with HTML 4.01 Strict and XHTML 1.1 (with an "application/xhtml+xml" MIME type HTTP
// header for all capable browsers - i.e. for all tested browsers except Internet Explorer and
// Opera 7.2x):
//
//   Tested and works (during page loading and when page is loaded) with:
//
//   Internet Explorer (Trident): 5.01, 5.55, 6.0, 7.0, 8.0
//   Opera (Presto): 7.2, 7.22, 7.23, 7.5, 8.0, 8.5, 8.53, 9.0, 9.23, 9.24, 9.25, 9.26, 9.27, 9.5, 9.51, 9.52, 9.6, 9.61, 9.62, 9.63, 9.64, 10.0 alpha 1
//   Opera Mini (Presto): basic/advanced: 1.2.2960, 1.2.3214, 2.0.4062, 2.0.4509, 3.1.7196, 3.1.10423, advanced: 4.0.9751, 4.0.10406, 4.1.11313, 4.1.11355, 4.2.13212
//   Safari (AppleWebKit): 3.0, 3.0.4, 3.1, 3.1.1, 3.1.2, 3.2, 3.2.1, 4.0 public beta
//   Chrome (AppleWebKit/V8): 0.2.149.30, 0.3.154.9, 0.4.154.33, 1.0.154.36, 1.0.154.53, 2.0.169.1 beta
//   Konqueror (KHTML): 3.5.8, 3.5.9, 4.0.80, 4.1.0, 4.1.1, 4.1.2, 4.1.3, 4.1.80, 4.2.0, 4.2.1, 4.2.2
//   Firefox/IceWeasel (Gecko): 0.8, 0.9, 1.0, 1.0.4, 1.5, 1.5.0.12, 2.0.0.4, 2.0.0.14, 2.0.0.15, 2.0.0.18, 2.0.0.19, 2.0.0.20, 3.0, 3.0.4, 3.0.5, 3.0.8, 3.1 beta 3, 3.2 alpha 1 pre
//   Fennec (Gecko): 1.0 alpha 1
//   Camino (Gecko): 1.6.1, 1.6.5
//   Epiphany (Gecko): 2.14, 2.18, 2.2, 2.22
//   Flock (Gecko): 1.0.9, 1.1, 1.1.2, 1.2.1, 1.2.6, 2.0, 2.0.2
//   Galeon (Gecko): 1.3.20, 2.0.2, 2.0.4, 2.0.5, 2.0.6
//   K-Meleon (Gecko): 0.8, 0.8.2, 0.9, 1.0, 1.0.1, 1.0.2, 1.1, 1.1.2, 1.1.3, 1.1.4, 1.1.5, 1.1.6, 1.5.0, 1.5.1
//   Kazehakase (Gecko): 0.2.7, 0.4.2, 0.4.3, 0.5.4
//   Mozilla (Gecko): 1.7.8, 1.7.13
//   Netscape Navigator (Gecko): 7.1, 7.2, 8.0.2, 9.0.0.5, 9.0.0.6
//   SeaMonkey/IceApe (Gecko): 1.0.9, 1.1.5, 1.1.8, 1.1.9, 1.1.13, 2.0 alpha 2
//
//   Tested and does NOT work (neither during page loading nor when page is loaded) with:
//
//   Internet Explorer (Trident): 4.01-
//   Opera (Presto): 7.11 b-
//   Safari (AppleWebKit): 2.0.4-
//   K-Meleon (Gecko): 0.7 sp 1-
//   Netscape Navigator (Gecko): 7.0-

function IsMediaType(mediatypes) {

    function createEl(name)
    {
        var element = null;
        if(document.createElementNS)
            element = document.createElementNS('http://www.w3.org/1999/xhtml', name);
        else
            element = document.createElement(name);
        return element;
    }

    function addCSSRule(id, selectorText, declarations, mediatypes)
    {
        // Dodajemy nowy arkusz styli
        var styleElement = createEl('style');
        if(!styleElement)
            return;
        styleElement.id = id;
        styleElement.type = 'text/css';
        styleElement.rel = 'stylesheet';
        if(mediatypes)
            styleElement.media = mediatypes;
        document.getElementsByTagName('head')[0].appendChild(styleElement);

        // Najpierw probujemy "appendChild", bo Safari i Konqueror obsluguje takze "document.styleSheets",
        // ale wtedy zle dziala
        var node = null;
        try { // Opera, Safari, Konqueror
            node = document.createTextNode(selectorText+' { '+declarations+' }');
            styleElement.appendChild(node);

            // Workaround dla Opery < 7.5
            if((window.opera) && (!window.opera.version)) { // "window.opera.version" wprowadzono w Operze 7.6
                styleElement.removeChild(node);
                styleElement.appendChild(node);
            }
        } catch(err) { // IE, Firefox
            if(node)
                delete node;
            if(document.styleSheets) {
                var styleSheet = document.styleSheets[document.styleSheets.length-1]; // Ostatnio dodany arkusz styli (za pomoca styleElement)
                if(styleSheet)
                    if(styleSheet.insertRule)
                        styleSheet.insertRule(selectorText+' { '+declarations+' }', styleSheet.cssRules.length);
                    else if(styleSheet.addRule)
                        styleSheet.addRule(selectorText, declarations);
            }
        }
    }

    function removeCSSRule(id)
    {
        var node = document.getElementById(id);
        document.getElementsByTagName('head')[0].removeChild(node);
        delete node;
    }

    function updateCSSRulesForKHTML()
    {
//        var ignoreMe = document.documentElement.offsetWidth; // Unstable in Konqueror 4.0.8
        try {
            var linkList = document.getElementsByTagName('link');
            var i;

            for(i = 0; i < linkList.length; i++) {
                linkList[i].orig_disabled = linkList[i].disabled;
                linkList[i].disabled = true;
            }

            for(i = 0; i < linkList.length; i++)
                linkList[i].disabled = linkList[i].orig_disabled;
        } catch(err) {}
    }

    try {
        // KB262161 - dotyczy IE 4.0 .. IE 8.0
        if(document.createStyleSheet) // IE
            if(document.styleSheets.length >= 30)
                return -1;

        addCSSRule('mediaInspector_rule1', '#mediaInspector', 'visibility: hidden; color: #000000;');
        addCSSRule('mediaInspector_rule2', '#mediaInspector', 'visibility: hidden; color: #FFFFFF;', mediatypes);

        // Element z id=mediaInspector musi byc widoczny (on ani zaden element nadrzedny nie moze
        // miec stylu "display: none" ani nie moze znajdowac sie w sekcji "head"), inaczej ustawianie
        // koloru nie bedzie zgodne ze standardem (i nie bedzie dzialac pod Safari ani Konquerorem);
        // wyjatek czynimy dla Opery < 9.5 ze wzgledu na wystepujacy w niej blad: jesli plik zostal
        // zaladowany przez protokol HTTP (ale nie FILE), to:
        //
        // (1) W Operze < 9.0 w przypadku uzycia podczas ladowania strony (ale nie po jej zaladowaniu)
        //     wlasciwosci "currentStyle" na elemencie zawartym w sekcji "body" (ale nie "head"), po
        //     odswiezeniu strony (F5) styl dla "body" z pliku CSS jest czasem losowo ignorowany
        //     (inny workaround: zadeklarowac styl dla "body" w naglowku pliku HTML w tagu "style")
        // (2) W Operze < 9.5 w przypadku uzycia podczas ladowania strony (ale nie po jej zaladowaniu)
        //     wlasciwosci "currentStyle" na elemencie zawartym w sekcji "body" (ale nie "head"), po
        //     odswiezeniu strony (F5) styl dla "body" z pliku CSS jest czasem losowo ignorowany,
        //     jesli w pliku CSS wystepuje rownoczesnie styl dla "*" (inny workaround: zamiast stylu
        //     dla "*" uzywac stylu dla "body *")
        var node = null;
        var place = null;

        var buggy_opera = false;
        if(window.opera) {
            buggy_opera = true;
            if((window.opera.version) && (parseFloat(window.opera.version()) >= 9.5))
                buggy_opera = false;
        }

        if(buggy_opera) {
            place = document.getElementsByTagName('head')[0];
            if(place)
                node = createEl('link');
        } else {
            place = document.getElementsByTagName('body')[0]; // document.body nie dziala dla XHTML w Safari 3.0.4-
            if(place)
                node = createEl('div');
        }
        if(node) {
            node.id = 'mediaInspector';
            place.appendChild(node);
        }

        if(node) {
            // Dla Konquerora - umozliwia prawidlowe dzialanie getComputedStyle, zanim zaaplikowane
            // zostana wszystkie arkusze styli
            updateCSSRulesForKHTML();

            var color = null;
            try {
                var mediaInspector = node;

                color = mediaInspector.style['color'];
                if(!color) {
                    if(mediaInspector.currentStyle)
                        color = mediaInspector.currentStyle['color'];
                    else if(window.getComputedStyle)
                        color = window.getComputedStyle(mediaInspector, null).getPropertyValue('color');
                }
            } catch(err) {}

            place.removeChild(node);
            delete node;
        }

        removeCSSRule('mediaInspector_rule1');
        removeCSSRule('mediaInspector_rule2');

        if(!color)
            return -1;

        color = color.replace(/[\s\t ]/gi, '').toUpperCase(); // \s does not work in Konqueror 4.1.1 and 4.1.2,
                                                              // so an additional space is needed
        if((color == '#FFFFFF') || (color == 'RGB(255,255,255)'))
            return 1;
        else if((color == '#000000') || (color == 'RGB(0,0,0)'))
            return 0;
        else
            return -1;
    } catch(err) { return -1; }
}