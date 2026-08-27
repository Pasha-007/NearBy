# Nearby

Event RSVP platform for Android.

## Stack

- Kotlin + Jetpack Compose
- MVVM architecture
- Firebase Auth (email/password) + Cloud Firestore (Native mode)
- Hilt for dependency injection
- KSP (not kapt) for annotation processing — this project uses AGP 9 with built-in Kotlin support, which is incompatible with kapt
- Kotlin coroutines/Flow for async and reactive state
- minSdk 24, targetSdk 36, compileSdk 37

## Branching model

- `main` — stable
- `develop` — integration branch
- `mvp` — current MVP work happens here; will merge into `develop` when done
- `feature/*` and `fix/*` — branch off `mvp` (or `develop`, once MVP is merged), rebase to stay updated, PR back in

## Firestore data model (rough)

### `users/{userId}`

```
{
  uid: string            // matches Firebase Auth UID, also the doc ID
  email: string
  displayName: string
  photoUrl: string?
  createdAt: timestamp
  updatedAt: timestamp
}
```

### `events/{eventId}`

```
{
  // eventId is the doc ID, not a stored field (Firestore @DocumentId)
  hostId: string          // ref to users/{userId}
  title: string
  description: string
  location: geopoint       // lat/lng, for map markers ("nearby" search/display)
  locationName: string     // human-readable text, e.g. "Central Park, NYC"
  startTime: timestamp
  endTime: timestamp
  capacity: number?        // null = unlimited
  rsvpCount: number        // denormalized count, kept in sync via transaction/Cloud Function
  visibility: string       // "public" | "private" | "invite_only"
  status: string           // "draft" | "published" | "cancelled"
  createdAt: timestamp
  updatedAt: timestamp
}
```

### `rsvps/{rsvpId}`

Top-level collection (not a subcollection) so a user's RSVPs can be queried across all events without a collection group query.

```
{
  rsvpId: string          // doc ID, e.g. `${eventId}_${userId}` for natural uniqueness
  eventId: string         // ref to events/{eventId}
  userId: string          // ref to users/{userId}
  status: string          // "going" | "maybe" | "declined" | "waitlisted"
  respondedAt: timestamp
  updatedAt: timestamp
}
```

**Relationships**

- `events.hostId` → `users.uid` (one host per event)
- `rsvps.eventId` → `events.eventId`, `rsvps.userId` → `users.uid` (many-to-many join between users and events)
- Composite doc ID (`eventId_userId`) on `rsvps` prevents duplicate RSVPs and enables a direct `get()` instead of a query when checking "did this user RSVP to this event."

**Indexes to plan for**

- `rsvps`: composite index on `(eventId, status)` for attendee lists per event
- `rsvps`: composite index on `(userId, respondedAt)` for a user's RSVP history
- `events`: composite index on `(visibility, startTime)` for public upcoming-events feeds
