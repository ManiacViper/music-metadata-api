# music-metadata-api
this is a prototype app for music metadata that will be used by a ui

## Running the app
```
sbt run
```
OR
```
./start.sh
```
## Initial Proposal Architecture for Production
[for architecture click here](https://www.canva.com/design/DAGTqGMEhDs/vaRYGMDAFfl8AQdXeSUJRg/edit?utm_content=DAGTqGMEhDs&utm_campaign=designshare&utm_medium=link2&utm_source=sharebutton)

## REST API REQUEST EXAMPLES
Host: localhost:8080
### save new track
POST /newtrack
```json
{
  "title" : "kens new title",
  "genre" : "Disco",
  "lengthInSeconds" : 245,
  "artistId" : "916e2cff-a76a-45f5-b373-c49d1c46828f" //only new tracks associated to an artist that exists in the system will be saved, please Check ArtistRepository for the full list of artist ids
}
```
### save new aliases for artist - as its just one field of a resource PATCH was used instead of PUT
PATCH /artist/5457804f-f9df-47e1-bc2b-250dceef9093
```json
{
  "aliases": ["some-new-one", "another-new-one"]
}
```

### tracks for an artist
GET /tracks/916e2cff-a76a-45f5-b373-c49d1c46828f

### artist of the day
GET /artist/daily/2024-10-25

