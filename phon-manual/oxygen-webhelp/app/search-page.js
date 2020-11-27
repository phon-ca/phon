/**
 * Load the libraries for the Search page.
 */
define(["require", "config"], function() {
    require(['search'], function() {
        require([
            'polyfill',
            'menu',
            'searchAutocomplete',
            'webhelp',
            'codeblock',
            'top-menu',
            'template-module-loader'
        ]);
    });
});