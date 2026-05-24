# Instrucciones para conectar con Firestore

## 1. Agregar dependencias en build.gradle (app)
```
dependencies {
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:32.7.0')
    implementation 'com.google.firebase:firebase-firestore-ktx'
    implementation 'com.google.firebase:firebase-auth-ktx'

    // ViewModel y LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.7.0'
}
```

## 2. Dónde va cada archivo
```
app/src/main/java/com/tuapp/
├── model/
│   └── Event.kt
├── repository/
│   └── EventRepository.kt
├── viewmodel/
│   └── EventViewModel.kt
├── adapter/
│   └── EventAdapter.kt
└── ui/
    ├── EventListFragment.kt
    ├── CreateEventFragment.kt
    └── EventDetailFragment.kt
```

## 3. Estructura en Firestore
```
events/          ← colección principal
  {eventId}/     ← documento de cada evento
    title
    description
    day
    month
    startTime
    endTime
    location
    organizerId
    organizerName
    category
    attendeesCount
    attendees/   ← subcolección de asistentes
      {userId}/
        userId
        userName
        userEmail
```

## 4. Agregar ProgressBar en los XML
En fragment_event_list.xml y fragment_create_event.xml agrega esto antes de cerrar el layout:
```xml
<ProgressBar
    android:id="@+id/progressBar"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:layout_gravity="center"
    android:visibility="gone" />
```
