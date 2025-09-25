package stoneframe.serena.gui.util;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.function.BiConsumer;

public class SpeechRecognizerUtil
{
    public static void setup(
        Activity activity,
        ImageButton speakButton,
        BiConsumer<String, String> callback)
    {
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity);
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "sv-SE");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false);
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

        speakButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!SpeechRecognizer.isRecognitionAvailable(activity))
                {
                    Toast.makeText(
                        activity,
                        "Speech recognition is not available on this device",
                        Toast.LENGTH_LONG).show();
                }
                else if (!hasMicrophonePermission())
                {
                    requestMicrophonePermission();
                }
                else
                {
                    speechRecognizer.startListening(speechRecognizerIntent);
                }
            }

            private boolean hasMicrophonePermission()
            {
                return ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED;
            }

            private void requestMicrophonePermission()
            {
                ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    1);
            }
        });

        speechRecognizer.setRecognitionListener(new RecognitionListener()
        {
            @Override
            public void onReadyForSpeech(Bundle params)
            {
                callback.accept("", "Ready...");
            }

            @Override
            public void onBeginningOfSpeech()
            {
                callback.accept("", "Listening...");
            }

            @Override
            public void onRmsChanged(float rmsdB)
            {
            }

            @Override
            public void onBufferReceived(byte[] buffer)
            {
            }

            @Override
            public void onEndOfSpeech()
            {
                callback.accept("", "Processing...");
            }

            @Override
            public void onError(int error)
            {
                callback.accept("", "");

                String text = error + " Error: " + getErrorMessage(error);

                Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onResults(Bundle results)
            {
                List<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                if (matches != null)
                {
                    String text = matches.get(0);

                    callback.accept(capitalizeFirstLetter(text), "");
                }
            }

            @Override
            public void onPartialResults(Bundle partialResults)
            {
            }

            @Override
            public void onEvent(int eventType, Bundle params)
            {
            }

            private @NonNull String getErrorMessage(int error)
            {
                switch (error)
                {
                    case SpeechRecognizer.ERROR_AUDIO:
                        return "Audio recording error";
                    case SpeechRecognizer.ERROR_CLIENT:
                        return "Client side error";
                    case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                        return "Insufficient permissions";
                    case SpeechRecognizer.ERROR_NETWORK:
                        return "Network error";
                    case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                        return "Network timeout";
                    case SpeechRecognizer.ERROR_NO_MATCH:
                        return "No match";
                    case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                        return "Recognizer busy";
                    case SpeechRecognizer.ERROR_SERVER:
                        return "Server error";
                    case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                        return "No speech input";
                    case SpeechRecognizer.ERROR_LANGUAGE_NOT_SUPPORTED:
                        return "Language not supported";
                    default:
                        return "Unknown error";
                }
            }

            private String capitalizeFirstLetter(String text)
            {
                if (text == null || text.isEmpty())
                {
                    return text;
                }

                return text.substring(0, 1).toUpperCase() + text.substring(1);
            }
        });
    }
}
