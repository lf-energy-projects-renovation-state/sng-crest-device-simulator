# Sng Crest Device Simulator

The simulator acts as a coap device, sends alarm messages and is able to handling a PSK set command.

## Flow

1. The simulator sends a coap alarm message to the coap-http proxy, from a `while(true)` loop
   in a separate virtual thread, started from the `Simulator.kt` class, that
   extends `CommandLineRunner`.
2. If the response received contains a `PSK:SET` command, then:
    1. Handle the PSK change:
        1. The new key is saved with status `PENDING`.
        2. Send a success message.
        3. The pending key gets status `ACTIVE` and the previous active key gets status `INACTIVE`.
    2. If an error occurs during the PSK change handling:
        1. Send a failure message.
        2. If a pending key was already saved, set the status of this key as `INVALID`.
   3. The thread sleeps for `simulatorProperties.sleepDuration` seconds and then the flow starts
      again.
   
