package com.mike_caron.equivalentintegrations;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldSavedData;

import java.util.HashMap;
import java.util.UUID;

public class OfflineEMCWorldData extends WorldSavedData
{
    public static final String IDENTIFIER = "EquivalentIntegrationsOfflineData";

    private HashMap<UUID, Double> cachedEMCValues = null;

    public OfflineEMCWorldData() {
        super(IDENTIFIER);
    }

    public double getCachedEMC(UUID uuid)
    {
        if(cachedEMCValues.containsKey(uuid))
        {
            return cachedEMCValues.get(uuid);
        }
        return 0d;
    }

    public void setCachedEMC(UUID uuid, double d)
    {
        cachedEMCValues.put(uuid, d);
        markDirty();
    }

    public void clearCachedEMC(UUID uuid)
    {
        if(cachedEMCValues.containsKey(uuid))
        {
            cachedEMCValues.remove(uuid);
            markDirty();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        if(nbt.hasKey("players"))
        {
            NBTTagCompound players = nbt.getCompoundTag("players");
            cachedEMCValues = new HashMap<>();
            for(String id : players.getKeySet())
            {
                UUID uuid = UUID.fromString(id);
                double d = players.getDouble(id);
                cachedEMCValues.put(uuid, d);
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound players = new NBTTagCompound();
        for(UUID uuid : cachedEMCValues.keySet())
        {
            players.setDouble(uuid.toString(), cachedEMCValues.get(uuid));
        }
        compound.setTag("players", players);

        return compound;
    }

    public static OfflineEMCWorldData get(World world)
    {
        OfflineEMCWorldData data = (OfflineEMCWorldData)world.loadData(OfflineEMCWorldData.class, IDENTIFIER);
        if(data == null)
        {
            data = new OfflineEMCWorldData();
            world.setData(IDENTIFIER, data);
        }
        return data;
    }
}
