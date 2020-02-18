Installation on http://www.predict.nhs.uk/
==========================================

## Installing the web content
The Document root on this server is at `/var/www/html`. This should
contain everything you need and can be copied to your own document root.
Copy the contents of this directory to your own document root.

## Installing redirects
On this server, the necessary redirects are in
`/etc/apache2/sites-enabled/000-default.conf`.

Here's what they look like:
```
        <Directory /var/www/html/predict_v2.1>
                <IfModule mod_rewrite.c>
                          RewriteEngine on
                          RewriteBase /predict_v2.1/


                          RewriteRule ^.*/assets/(.*)$ assets/$1 [L,QSD]
                          RewriteRule ^.*/js/(.*)$ js/$1 [L,QSD]
                          RewriteRule ^.*/css/(.*)$ css/$1 [L,QSD]

                          RewriteCond %{REQUEST_FILENAME} !-f
                          RewriteRule ^(.*)$ index.html [L,QSA]

                </IfModule>
        </Directory>
```

It may be possible to run these from a .htaccess file, but in that case
you will need the server to have something like an `AllowOverride` settings.

## Why these redirects are necessary
The predict_v2.1 directory contains a single page app located at
/predict_v2.1/index.html. This has its own javascript
routing engine which echoes 'clean' URLs like
`/predict_v2.1/about/overview/howpredictworks` to the address bar for
the page views within the app.

If a user copies or bookmarks this URL and enters it into a browser, she
would expect that same view to load. To do so, the URL must be redirected
by the server to `/predict_v2.1/index.html`. This happens in the last two
Rewrite lines.

During development we run the site with hash-fragment URLs rather than
clean URLs, and we do not redirect. The page above would show
`/predict_v2.1#/about/overview/howpredictworks` in the address bar, and
so js, css, and image assets are accessed using URLs acting relative to
the base /predict_v2.1 directory.

On the  production server, these relative URLs must now work with clean URLs,
so we need to map URLs like `/predict_v2.1/about/overview/howpredictworks/js`
to /predict_v2.1/js. The first three redirects do this.

## Why Clean URLs?

We use clean URLs mostly because using hash-fragments for routing purposes
would make screen readers more difficult to understand. Also, Google Analytics
indexes can then index the single page app in the same
way as it indexes standard html pages.


## Post installation checks

1. Check the site looks like staging.wintoncentre.uk.
2. Check /predict_v2.1 looks like staging.wintoncentre.uk/predict_v2.1
3. Check /predict_v2.1/tools is working and generating results when all
inputs have been filled in.
4. Check the URLs that appear in the address bar while running
/predict_v2.1 can also be entered in the address bar and that they then
take you to the expected page view.
5. Check that Google analytics are correct and working in all root HTML
files (including /predict_v2.1/index.html). We haven't checked this
since we have not been running at the coorect www.predct.nhs.uk URL
during development.


