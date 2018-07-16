# Technical Details

Integrating with the ProjectE API was very interesting, but at times frustrating. Here, I want to document some of the
things I had to do to make this mod work properly. This is for educational purposes!

## EMC Storage
Equivalent Integrations is meant for automation, which means that it needs to work whether a given player is online or
not. However, ProjectE explicitly does not support mutating offline players, and will complain loudly if you try. While
it would be possible to work around this by manually modifying ProjectE's data structures, I felt that was not a robust
enough solution, since it would break if ProjectE decided to change these.

Instead, I wrote an abstraction layer on top of ProjectE, and exposed it as a Forge Capability, IEMCManager. This
capability will delegate to ProjectE in cases when the player is online and available, but will fall back to managing
the EMC itself if the player is offline. It also monitors for when a player logs back in, and will sync the EMC back
to ProjectE once the player is mutable again.

Lastly, there were no events in ProjectE to listen for changes in a player's EMC. So, the capability monitors that as
well, and exposes an event that can be listened to. This is far more efficient than every single machine polling
periodically for changes.

I use this capability in several places, and I expect to lean on it heavily in the future if I add more machines that
deal with EMC.

## Transmutation Chamber
This block exposes the IItemHandler capability. It fulfills this interface by exposing a virtual inventory based on the
player's knowledge and current EMC values. It precalculates what this inventory will look like so that when it's
accessed (which might be several times per tick), it can quickly fill requests. Each stack exposed will be the number
that you could craft if you turned all your EMC into just that. Eg, if you have 1000 EMC, and know Cobblestone and Oak
Wood Planks, it will expose 1000 x Cobblestone and 250 x Oak Wood Planks (since they cost 1 and 4 EMC respectively).
Obviously, if you try to withdraw both, it will fail since you don't actually have enough EMC.

However, because it's a _virtual_ inventory, it doesn't really use slots in the same way as a chest or something. In
fact, it will completely ignore what slot someone tries to insert into, since it's just going to convert it into EMC
anyway.

That said, for compatability with other mods (who might expect slots to fill up), it also exposes 64 extra slots that
always register as completely empty.

Also, because they aren't real slots, it has no problem exposing that a slot contains, eg, 5000 x Cobblestone, or
100 x Lava Buckets, even though stacks are normally limited to 64, and Lava Buckets don't even stack! Fortunately,
this limitation only applies to actual concrete inventories, so it's no big deal.

Note that it actually has a second, real inventory. This is where it stores the Soulbound Talisman, etc. If you ask for
its inventory from, eg, it's West side, you will get the virtual inventory described above. However, if you ask instead
specifying no facing, then you will get the internal inventory instead. However, you probably don't want to do that, so
don't!

