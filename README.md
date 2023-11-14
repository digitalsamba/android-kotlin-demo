# DigitalSamba native app example

This example requires media devide permissions to be configured in `AndroidManifest.xml`, see `MEDIA DEVICE PROPERTIES`.

For this example we instantiate a Chrome WebView that loads a custom HTML file with the JS environment for embedded app.
the example we simply load the frame and utilise `WebView.evaluateJavascript()` to send  arbitrary commands against SDK handle.

See [docs](https://docs.digitalsamba.com/reference/sdk/digitalsambaembedded-class) for details on
available commands and payloads.
Native app exposes a `window.Android.receiveMessage` message to the JS runtime  to listen to embedded app events.
