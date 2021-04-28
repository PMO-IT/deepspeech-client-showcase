package de.pmoit.deepspeech.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.TargetDataLine;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;


/**
 * DeepSpeech Client. Uses standard microphone and translates the audio signal
 * into an ByteArrayOutputStream. That stream is sent to the DeepSpeech server.
 * The Server Responds with the translated string or an exception.
 * 
 * 
 * Mic input format:
 * 
 * PCM_SIGNED 16000.0 Hz, 16 bit, mono, 4 bytes/frame, little-endian
 * 
 * Curlpattern:
 * 
 * curl -X POST --data-binary @testfile.wav http://localhost:8080/stt
 * 
 */

public class DeepSpeechClient {

    private static final String SERVERADDRESS = "http://SERVERADDRESS:8080/stt";

    public String transcribe() throws Exception {

        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        if ( ! AudioSystem.isLineSupported(info)) {
            throw new Exception("Mikrofon ist nicht verfügbar!");
        }
        TargetDataLine line = ( TargetDataLine ) AudioSystem.getLine(info);
        line.open(format, line.getBufferSize());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] data = new byte[line.getBufferSize() / 4];

        System.out.println("StartLine");
        line.start();
        long startTime = System.currentTimeMillis();
        while ( ( System.currentTimeMillis() - startTime ) < 2000) {
            int numBytesRead = line.read(data, 0, data.length);
            out.write(data, 0, numBytesRead);
        }
        System.out.println("ende");

        ByteArrayOutputStream byos = createAudioOutputStream(line, out);
        return sendDataToServer(byos);
    }

    private ByteArrayOutputStream createAudioOutputStream(TargetDataLine line, ByteArrayOutputStream out)
        throws IOException {
        int frameSizeInBytes = line.getFormat().getFrameSize();
        byte audioBytes[] = out.toByteArray();

        AudioInputStream audioInputStream = new AudioInputStream(new ByteArrayInputStream(out.toByteArray()), line
            .getFormat(), audioBytes.length / frameSizeInBytes);
        ByteArrayOutputStream byos = new ByteArrayOutputStream();

        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, byos);
        audioInputStream.close();
        return byos;
    }

    /**
     * Sends ByteArrayOutputStream to the DeepSpeech server and recievs an
     * inputstream.
     * 
     * @param byos
     * @return Spoken result as String
     * @throws IOException
     * @throws ClientProtocolException
     */
    private String sendDataToServer(ByteArrayOutputStream byos) throws IOException, ClientProtocolException {
        HttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(SERVERADDRESS);
        ByteArrayEntity bae = new ByteArrayEntity(byos.toByteArray());
        httppost.setEntity(bae);

        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            try (InputStream instream = entity.getContent()) {
                return IOUtils.toString(instream, "UTF-8");
            }
        }
        return "Kein Ergebnis";
    }

}
