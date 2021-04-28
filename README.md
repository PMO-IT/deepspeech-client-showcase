# deepspeech-client-showcase
deepspeech-client as showcase

# Description
This is a showcase Java client for Mozillas DeepSpeech speech-to-text. The microphone uses PCM_SIGNED 16000.0 Hz, 16 bit, mono, 4 bytes/frame, little-endian. No particular exception handling is present, since this ist just a showcase version of the client for an article. 

Insert the server address in the SERVERADDRESS constant and use the transcribe method. The microphone listens 2 seconds, afterwards it sends the signal form of a ByteArrayOutputStream to the DeepSpeech server. The server responds with either an input stream or an exception. 
The sendDataToServer method translates the recieved inputStream to a string with UTF-8 charset.

## Configure Serveraddress:
```java
private static final String SERVERADDRESS = "http://SERVERADRESS:8080/stt";
```

## Example:
```java
public static void main(String[] args) throws IOException {
	DeepSpeechClient client = new DeepSpeechClient();
	System.out.println(client.transcribe());
}
```
