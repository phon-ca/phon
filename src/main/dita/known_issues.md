# Known Issues

Some common issues with Phon are listed in the table below. To report a problem goto http://github.com/phon-ca/phon/issues.

| Issue | Resolution |
| --- | --- |
| Phon runs out of memory generating large reports | Increase the amount of memory available to Phon using the method outlined below |
| Phon freezes when loading .wav file in session editor. | If the .wav file was taken from a digital recording device it may be missing header information.  Re-encode .wav file before using in Phon (see below.) |
| Unable to use IPA input fields after stacking multiple combining diacritics. | No known solution at this time. |
| Unable to view reports/application logs. Blank window displayed. | If running on a managed Windows system running AppLocker or other security software required binaries may be blocked from execution.  Please see your system administrator. |

## Increase memory available to Phon

The ```vmoptions``` file can be found in the following location:

 * C:\\Program Files\\Phon\\vmoptions.txt (windows)

 * /Applications/Phon.app/Contents/Macos/vmoptions.txt (macos)

Modify the ```vmoptions.txt``` file, change the line which says

```
-Xmx4098m
```

to another value, for example 8GB

```
-Xmx8192m
```

## Re-encode Audio File for Phon

Using the latest version of [Audacity](https://www.audacityteam.org/):
 
 * Open your original file
 * Select menu ```File``` > ```Export as WAV```
 * Choose either ```WAV (Microsoft) signed 16-bit PCM format``` or ```WAV (Microsoft) signed 24-bit PCM format```
 * Link your Phon transcript file to this newly-created WAV file
