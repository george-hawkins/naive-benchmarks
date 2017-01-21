Naive benchmarks
================

A set of simple, but fairly naive, benchmarks that attempt to exercise the following:

* processor
* memory
* disk
* networking.

For each of the above some fairly arbitrary numbers are generated.

They are not really intended for comparing different systems but for comparing the performance of a single system as changes are made to it.

E.g. if one is planning to make a number of dramatic changes to a system one can run the benchmarks before the changes and then run them at various points while making the changes to see if any given change has impacted the numbers.

The benchmarks are controlled by the [`benchmarks.conf`](benchmarks.conf) file found in the root directory.

The default version of `benchmarks.conf`, that comes with this repository, comes with a what are suggested minimum values for the number of cycles for each benchmark and the amount of e.g. disk space etc. to use.

Using lower values tends to produce overly variable results. These values are obviously system dependent and one should experiment with them.

But really it's expected that one increases the values to e.g. use all available system memory and to run for as long as one has patience.

On a small embedded system like a Raspberry Pi using all memory and all disk is reasonable - on systems with larger disks it probably doesn't make sense to e.g. ask the disk benchmark to fill the entire disk.

For the networking benchmark one needs another system with which the benchmark system can communicate.

The benchmarks here come with a simple network server that needs to be run on that other system and the `network.server` entry in `benchmarks.conf` needs to be updated to specify the hostname or IP address of that system.

To build the project:

    $ mvn clean compile

This needs to be done on both the system being benchmarked and the system that will run the simple network server.

Then to start the network server:

    $ mvn exec:java@network-server

Once the log output shows the network server is ready the benchmarks can be run:

    $ mvn exec:java@benchmarks

The network server can be left running - it does not need to be restarted for each run of the benchmarks. To kill the network server just use `^C`.

Note: you need at least Maven 3.3.1 in order to specify executions by ID (`network-server` and `benchmarks` above). See <http://stackoverflow.com/a/8252214/245602>.

Memory
------

You should give the benchmarks all the free memory available - the OS may otherwise use free memory for caching that can significantly affect some results.

On Linux you can see how much memory is free like so:

    $ free -m
                  total        used        free      shared  buff/cache   available
    Mem:          16000       12589        2070         796        1341        2261
    Swap:          8143        2119        6024

Here we see that 2070MiB are free.

To give nearly all of this to the benchmarks set `MAVEN_OPTS` before running `mvn` like so:

    $ export MAVEN_OPTS='-Xms1900m -Xmx1900m'

Use `-Xmx` to set the upper bound and use `-Xms` with the same value so that the benchmarks grab all this space immediately.

Note that if you don't set `-Xmx` the default upper limit may be much higher than the actual amount of free memory. When the benchmarks start up one of the first things logged is the amount of free memory that the JVM **thinks** is available to it.

Important: you have to ensure the value of `memory.size` in `benchmarks.conf` is less than the value specified with `-Xmx` otherwise you'll get a OOM exception. `memory.size` is the limit of how much memory you want the memory benchmark to exercise - it doesn't need to be any where near the value of `-Xmx` but there's not much reason not to exercise as much memory as is available.

Notes
-----

It would be interesting to see if there's any difference in numbers between using OpenJDK and the Oracle JDK - though presumably any differences between the JDKs are in areas other than the raw data churning that these benchmarks do, e.g. MPEG encoding or such like.

Note that `-server` is implicit for all 64-bit JDKs.
