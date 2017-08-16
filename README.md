# Spring Batch examples

This project is based on 

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