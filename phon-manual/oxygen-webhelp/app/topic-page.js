/**
 * Load the libraries for the DITA topics pages.
 */
define(["require", "config"], function() {
    require([
        'polyfill',
        'menu',
        'toc',
        'searchAutocomplete',
        'webhelp',
        'codeblock',
        'top-menu',
        'search-init',
        'expand',
        'permalink',
        'image-map',
        'template-module-loader',
        'bootstrap'
    ]);
});