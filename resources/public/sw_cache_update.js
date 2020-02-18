let CACHE = 'cache-and-update';
let VERSION_CUR = 'v1.027';
// Internal tested. First live version
let LATEST_CACHE_ID = CACHE + '--' + VERSION_CUR;
console.log(VERSION_CUR);

// On install, cache some resources.
self.addEventListener('install', function(evt) {
    // console.log('The service worker is being installed. ');
    console.log('The service worker is being installed. ' + LATEST_CACHE_ID);
    self.skipWaiting()

    // Ask the service worker to keep installing until the returning promise
    // resolves.
    evt.waitUntil(precache());
});

// On fetch, use cache but update the entry with the latest contents
// from the server.
self.addEventListener('fetch', function(evt) {
    // console.debug('The service worker is serving the asset.');
    // You can use `respondWith()` to answer immediately, without waiting for the
    // network response to reach the service worker...
    evt.respondWith(fromCache(evt.request));
    // ...and `waitUntil()` to prevent the worker from being killed until the
    // cache is updated.
    evt.waitUntil(update(evt.request));
});

// different fetch
// self.addEventListener('fetch', function(event) {
//     event.respondWith(
//         // Try the cache
//         caches.match(event.request).then(function(response) {
//             return response || fetch(event.request);
//         }).catch(function() {
//             //Error stuff
//         })
//     );
// });


// Open a cache and use `addAll()` with an array of assets to add all of them
// to the cache. Return a promise resolving when all the assets are added.
function precache() {
    // return caches.open(CACHE).then(function (cache) {
    return caches.open(LATEST_CACHE_ID).then(function (cache) {
        // Debugging temp code to clear cache
        // console.log('cache in precache')
        // console.log(cache)
        // caches.delete('cache-and-update--v1.026')
        return cache.addAll([
            '/',
            '/tool',
            '/about/overview/overview',
            '/about/overview/whoisitfor',
            '/about/overview/howpredictworks',
            '/about/overview/whobuiltpredict',
            '/about/overview/about',
            '/about/technical/technical',
            '/about/technical/history',
            '/about/technical/preversions',
            '/about/technical/publications',
            '/about/faqs',
            '/legal/disclaimer',
            '/legal/algorithm',
            '/legal/privacy',
            '/index.html',
            '/manifest.json',
            '/assets/favicon.png',
            '/assets/browser-ie.png',
            '/assets/browser-firefox.png',
            '/assets/browser-chrome.png',
            '/assets/browser-edge.png',
            '/assets/NHS.jpg',
            '/assets/tool-banner.png',
            '/assets/icon-imagery.png',
            // '/assets/print-footer.png',
            '/assets/phe-neg.png',
            // '/assets/urology.png',
            '/assets/ucs-winton-transparent.png',
            '/assets/logo-pos-al.png',
            '/assets/logo_breast_192.png',
            '/assets/logo_breast_512.png',
            '/assets/faq-icon.png',
            '/assets/tool-icon.png',
            '/assets/touch-icon-ipad.png',
            '/assets/touch-icon-ipad-retina.png',
            '/assets/touch-icon-iphone.png',
            '/assets/touch-icon-iphone-retina.png',
            '/assets/bullet-plus.png',
            '/assets/AJCC_logo_RGB.png',
            // '/js/analytics.js',
            '/js/autotrack.js',
            '/js/mediatypechecker.js',
            '/js/jquery.smartmenus.min.js',
            '/js/jquery.smartmenus.bootstrap.min.js',
            '/js/compiled/predict3.js',
            '/css/jquery.smartmenus.bootstrap.css',
            '/css/tooling_styles.css',
            '/css/ionicons.min.css',
            '/css/bootstrap-select.css',
            '/css/bootstrap.min.css',
            '/css/font-awesome.min.css',
            '/js/jquery-3.3.1.min.js',
            '/js/bootstrap.min.js',
            '/assets/graph-icon.png',
            '/assets/patient-icon.png',
            '/fonts/fontawesome-webfont.woff2',
            '/fonts/fontawesome-webfont.woff',
            '/assets/graph-icon.png',
            '/assets/patient-icon.png',
            '/assets/book-icon.png',
            '/assets/print-footer.png',
            '/assets/white-logo.png',
            '/assets/ucs-winton-blue.png',
            '/assets/phe-pos.png',
        ]);
    })
        .catch(function(err) {
            // console.log('Failed to fetch in prefetch', err)
        });
}

// // Tutorial
addEventListener('activate', activateEvent => {
    activateEvent.waitUntil(
        caches.keys().then(keyList => Promise.all(keyList.map(key => {
            // console.log("key from activate")
            // console.log(key)
            if (key !== LATEST_CACHE_ID) {
                console.debug("New key found. Deleting old cache")
                return caches.delete(key);
            }
        })))
        .then(() => {
            // console.debug('Activating now via self.skipWaiting()')
            return self.skipWaiting()
        })
    );
});


// Open the cache where the assets were stored and search for the requested
// resource. Notice that in case of no matching, the promise still resolves
// but it does with `undefined` as value.
function fromCache(request) {
    // console.debug('fromCache - request')
    // console.debug(request)

    return caches.open(LATEST_CACHE_ID).then(function (cache) {
        return cache.match(request).then(function (matching) {
            // console.debug('fromCache - matching')
            // console.debug(matching)
            // return matching || Promise.reject('no-match');
            return matching || fetch(request);
        }).catch(function(res) {
            // console.debug("Error fetching not found content in cache.")
            // console.debug(res)
        });
    });
}

// Update consists in opening the cache, performing a network request and
// storing the new response data.
function update(request) {
    return caches.open(LATEST_CACHE_ID).then(function (cache) {
        return fetch(request).then(function (response) {
            return cache.put(request, response);
        }).catch(function(err) {
            // console.debug('update fetch error. ')
            // console.debug(err)
        });
    });
}

