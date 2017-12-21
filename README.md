# D-BAS Analytics


### Development mode

To start the Figwheel compiler, navigate to the project folder and run the following command in the terminal:

```
lein figwheel
```

Figwheel will automatically push cljs changes to the browser.
Once Figwheel starts up, you should be able to open the `public/index.html` page in the browser.
(defaults to `localhost:3449)


### Building for production

```
lein clean
lein package
```
