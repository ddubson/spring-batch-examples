# Spring Batch examples

Spring Batch framework use case examples with Kotlin

### Samples (git tags):

###### Basics

- `01-basic-job` - basic multi-step job example
- `02-first-last-flows` - flow suffixed and prefied with a step
- `03-...`
- `04-decision-based-stepping` - conditional (decision based) job stepping
- `05-nested-jobs` - nested parent-child jobs showcasing job within job structuring.
- `06-pre-post-listeners` - pre-chunk and after-chunk listeners.
- `07-job-parameters` - sending in job parameters to a job

###### Readers

- `08-stateless-item-reader-chunk-job` - read items in chunks from a list
- `09-reading-from-db` - read items in chunks from database
- `10-reading-from-fs` - read items in chunks from file system (csv format)
- `11-reading-from-xml` - read items in chunks from XML file
- `12-reading-from-multiple-fs` - read items in chunks from multiple files in FS
- `13-reading-stateful-cycle` - read items and track events during cycle in a stateful manner

###### Writers

- `14-writing-basic` - writing a basic list of ints to sysout
- `15-writing-items-to-db` - writing items to a jdbc database source
- `16-writing-items-to-fs` - writing items to a flat file
- `17-writing-items-to-xml` - writing items to an XML file
- `18-writing-to-multiple-files` - writing items to multiple files/formats

###### Processors

- `19-processing-basic` - processing an item, basic example
- `20-processing-filtering` - processing an item, filtering items
- `21-processing-validating` - processsing an item, validating and filtering
- `22-processing-chaining` - processing an item, chaining processors

###### Error and State Handling

- `23-restarting-jobs` - if job fails during processing, batch restarts from an offset
- `24-retrying-steps` - if step fails, retry N times until working
- `25-skipping-steps` - if step fails, skip the step that failed
- `26-listen-on-skips` - attach listeners to items that are skipped

###### Scaling Batch

- `27-multi-thread-step` - Multi-threaded step (each chunk is processed in its own thread)
- `28-async-processors` - AsyncItemProcessor - itemprocessor returns a Future, itemwriter unwraps resolved Future
- `29-local-partitioner` - Split step into 4 batches and process in a grid of 4
- `30-remote-partitioner` - Split step into multiple remote jvm's passing commands over RabbitMQ and channels
```
./gradlew clean build
java -jar -Dspring.profiles.active=slave build/libs/spring-batch-examples.jar -minValue=1 -maxValue=100000
java -jar -Dspring.profiles.active=master build/libs/spring-batch-examples.jar -minValue=1 -maxValue=100000
```

- `31-remote-chunking` - Split process chunks into remote JVMs (RabbitMQ)
```
./gradlew clean build
java -jar -Dspring.profiles.active=slave build/libs/spring-batch-examples.jar
java -jar -Dspring.profiles.active=master build/libs/spring-batch-examples.jar
```