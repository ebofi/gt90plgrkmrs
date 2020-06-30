var exec = require('cordova/exec');
/**
 * Callback function is a prompt to send a print message.
 * @param {string} message - The string will be sent to the printer.
 */
exports.printString = function (arg0, success, error) {
    exec(success, error, 'GcPrintPlugin', 'printString', [arg0]);
};
/**
 * Callback function is a prompt to send a print message.
 */
exports.printBmp = function (success, error) {
    exec(success, error, 'GcPrintPlugin', 'printBmpIntent');
};
/**
 * Callback function is a prompt to send a print message.
 * @param {uri}  - The storage location of the local picture or network address URL.
   eg.  file://storage/emulated/0/xxxx/xx.png
        content://com.android.providers.media.documents/document/image%3A16
        http://xxxxx/xxx.jpg
 */
exports.printBmpUri = function (arg0, success, error) {
    exec(success, error, 'GcPrintPlugin', 'printBmpUri', [arg0]);
};
/**
 * Callback function is a prompt to send a print message.
 * @param {url}  - The storage location of the local picture or network address URL.
			 eg.  file:///storage/emulated/0/xxxx/xxx.html
       			http://xxxxx/xxx.html
 	 @param {htmlString} - The text content of the HTML.
   		 eg.  <html>
   		 				<head><meta charset="utf-8">
        			<script type="text/javascript" src="cordova.js"></script>
        			</head><body>
        			<p>This is the print content</p>
        			</body>
   @param {width} - You can set the width of the web page.
   				(w>=0 && w<=384) - Defaults=384
   				(w=-1) - MATCH_PARENT
   				(w=-2) - WRAP_CONTENT
   				(w>384) - width
   @param {timeOutTime} - Page load timeout(s).Setting 0 is not enabled
   @param {bTimeOutPrint} - Whether to print when the timeout status occurs.
   @param {setCancelable} - Sets whether this dialog is cancelable with the.

 */
exports.printHtml = function (url,htmlString,width,timeOutTime,bTimeOutPrint,setCancelable,success, error) {
    exec(success, error, 'GcPrintPlugin', 'printHtml', [url,htmlString,width,timeOutTime,bTimeOutPrint,setCancelable]);
};
