package com.idat.pe.task_service.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.idat.pe.task_service.entity.Tarea;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Service
public class GoogleCalendarService {

    private static final String APPLICATION_NAME = "Task Service";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    private Calendar getCalendarService() throws Exception {
        final HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        
        InputStream in = GoogleCalendarService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new IOException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        
        return new Calendar.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public Event crearEvento(Tarea tarea) {
        try {
            Calendar service = getCalendarService();
            
            Event event = new Event()
                    .setSummary(tarea.getTitulo())
                    .setDescription(tarea.getDescripcion());

            // Convertir LocalDateTime a Google DateTime
            if (tarea.getFechaLimite() != null) {
                DateTime startDateTime = new DateTime(convertToDate(tarea.getFechaLimite()));
                EventDateTime start = new EventDateTime()
                        .setDateTime(startDateTime)
                        .setTimeZone("America/Lima");
                event.setStart(start);

                // Fin del evento 1 hora después
                DateTime endDateTime = new DateTime(convertToDate(tarea.getFechaLimite().plusHours(1)));
                EventDateTime end = new EventDateTime()
                        .setDateTime(endDateTime)
                        .setTimeZone("America/Lima");
                event.setEnd(end);
            } else {
                // Si no hay fecha, ponerlo para hoy
                 DateTime now = new DateTime(System.currentTimeMillis());
                 event.setStart(new EventDateTime().setDateTime(now));
                 event.setEnd(new EventDateTime().setDateTime(new DateTime(System.currentTimeMillis() + 3600000)));
            }

            event = service.events().insert("primary", event).execute();
            return event;
            
        } catch (Exception e) {
            System.err.println("Error creando evento en Google Calendar: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public Event actualizarEvento(Tarea tarea) {
        if (tarea.getGoogleEventId() == null) return crearEvento(tarea);
        
        try {
            Calendar service = getCalendarService();
            
            Event event = service.events().get("primary", tarea.getGoogleEventId()).execute();
            event.setSummary(tarea.getTitulo())
                 .setDescription(tarea.getDescripcion());

            if (tarea.getFechaLimite() != null) {
                DateTime startDateTime = new DateTime(convertToDate(tarea.getFechaLimite()));
                event.setStart(new EventDateTime().setDateTime(startDateTime).setTimeZone("America/Lima"));
                
                DateTime endDateTime = new DateTime(convertToDate(tarea.getFechaLimite().plusHours(1)));
                event.setEnd(new EventDateTime().setDateTime(endDateTime).setTimeZone("America/Lima"));
            }

            return service.events().update("primary", event.getId(), event).execute();
        } catch (Exception e) {
            System.err.println("Error actualizando evento en Google Calendar: " + e.getMessage());
            return null;
        }
    }

    public void eliminarEvento(String eventId) {
        if (eventId == null) return;
        try {
            Calendar service = getCalendarService();
            service.events().delete("primary", eventId).execute();
        } catch (Exception e) {
            System.err.println("Error eliminando evento en Google Calendar: " + e.getMessage());
        }
    }

    private Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
