/*
 * Copyright 2014 Alexander Chauncey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.censoredsoftware.infractions.bukkit.legacy.compat;

import com.censoredsoftware.infractions.bukkit.Database;
import com.censoredsoftware.infractions.bukkit.Infraction;
import com.censoredsoftware.infractions.bukkit.Infractions;
import com.censoredsoftware.infractions.bukkit.dossier.CompleteDossier;
import com.censoredsoftware.infractions.bukkit.dossier.Dossier;
import com.censoredsoftware.infractions.bukkit.evidence.Evidence;
import com.censoredsoftware.infractions.bukkit.legacy.InfractionsPlugin;
import com.censoredsoftware.infractions.bukkit.legacy.data.DataManager;
import com.censoredsoftware.infractions.bukkit.legacy.data.file.FileDataManager;
import com.censoredsoftware.infractions.bukkit.legacy.data.file.InfractionsFile;
import com.censoredsoftware.infractions.bukkit.legacy.data.file.YamlFileUtil;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public class LegacyDatabase implements Database {
    @Override
    public CompleteDossier getCompleteDossier(UUID playerId) throws NullPointerException {
        Dossier dossier = getDossier(playerId);
        if (dossier instanceof LegacyCompleteDossier) return (CompleteDossier) dossier;
        throw new NullPointerException("Incomplete dossier.");
    }

    @Override
    @Deprecated
    public CompleteDossier getCompleteDossier(String playerName) {
        UUID id = null;
        try {
            id = new UUIDFetcher(Lists.newArrayList(playerName)).call().get(playerName);
        } catch (Exception ignored) {
        }

        if (id != null) {
            Dossier dossier = getDossier(id);
            if (!(dossier instanceof CompleteDossier)) {
                Validate.notNull(dossier, "DOSSIER");
                Validate.notNull(id, "ID");
                Validate.notNull(playerName, "PLAYER NAME");
                dossier = dossier.complete(playerName);
                DataManager.getManager().getMapFor(LegacyDossier.class).put(dossier.getId(), dossier);
            }
            return (CompleteDossier) getDossier(id);
        }
        throw new NullPointerException("No such player exists.");
    }

    @Override
    public CompleteDossier getCompleteDossier(Player player) {
        LegacyCompleteDossier dossier = (LegacyCompleteDossier) getCompleteDossier(player.getName());

        // Check for corrupt ID
        if (!dossier.confirmedValid) {
            try {
                if (player.getUniqueId().equals(dossier.getId())) {
                    dossier.confirmedValid = true;
                } else {
                    InfractionsFile file = ((FileDataManager) DataManager.getManager()).yamlFiles.get(LegacyDossier.class);
                    Configuration config = YamlFileUtil.getConfiguration(file.getDirectoryPath(), file.getFullFileName());
                    ConfigurationSection data = config.getConfigurationSection(dossier.getId().toString());

                    Set<Infraction> toAdd = Sets.newHashSet();
                    for (Infraction infraction : dossier.getInfractions()) {
                        dossier.acquit(infraction);
                        infraction.setPlayerId(player.getUniqueId());
                        toAdd.add(infraction);
                    }

                    Infractions.removeDossier(dossier);
                    LegacyCompleteDossier newDossier = (LegacyCompleteDossier) LegacyCompleteDossier.of(player.getUniqueId(), data);
                    Infractions.addDossier(newDossier);

                    for (Infraction infraction : toAdd) {
                        newDossier.cite(infraction);
                    }

                    // Log it
                    InfractionsPlugin.getInst().getLogger().warning("UUID FOR \'" + dossier.getLastKnownName() + "\' IS INVALID.");
                    InfractionsPlugin.getInst().getLogger().warning("- OLD ID: " + dossier.getId().toString());
                    InfractionsPlugin.getInst().getLogger().warning("- ACTUAL: " + player.getUniqueId().toString());

                    dossier = newDossier;
                }
            } catch (Exception ignored) {
            }
        }

        return dossier;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<CompleteDossier> getCompleteDossiers(final InetAddress address) {
        return (Set<CompleteDossier>) (Set) Sets.filter(allDossiers(), new Predicate<Dossier>() {
            @Override
            public boolean apply(Dossier dossier) {
                return dossier instanceof CompleteDossier && ((CompleteDossier) dossier).getAssociatedIPAddresses().contains(address);
            }
        });
    }

    @Override
    public Dossier getDossier(UUID playerId) {
        if (playerId == null) return null;
        LegacyDossier dossier = new LegacyDossier(playerId);
        Dossier answer = (Dossier) DataManager.getManager().getMapFor(LegacyDossier.class).putIfAbsent(playerId, dossier);
        return answer != null ? answer : dossier;
    }

    @Override
    public Dossier getDossier(String playerName) {
        UUID id = null;
        try {
            id = new UUIDFetcher(Lists.newArrayList(playerName)).call().get(playerName);
        } catch (Exception ignored) {
        }

        if (id != null)
            return getDossier(id);
        throw new NullPointerException("No such player exists.");
    }

    @Override
    public void addDossier(Dossier dossier) {
        DataManager.getManager().getMapFor(LegacyDossier.class).put(dossier.getId(), dossier);
    }

    @Override
    public void removeDossier(Dossier dossier) {
        DataManager.getManager().getMapFor(LegacyDossier.class).remove(dossier.getId());
    }

    @Override
    public void removeDossier(UUID playerId) {
        DataManager.getManager().getMapFor(LegacyDossier.class).remove(playerId);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<Dossier> allDossiers() {
        return Sets.newHashSet((Collection<Dossier>) (Collection) DataManager.getManager().getAllOf(LegacyDossier.class));
    }

    @Override
    public Set<Infraction> allInfractions() {
        Set<Infraction> infractions = Sets.newHashSet();
        for (Dossier dossier : allDossiers())
            infractions.addAll(dossier.getInfractions());
        return infractions;
    }

    @Override
    public Set<Evidence> allEvidence() {
        Set<Evidence> evidence = Sets.newHashSet();
        for (Infraction infraction : allInfractions())
            evidence.addAll(infraction.getEvidence());
        return evidence;
    }

    @Override
    public Plugin getPlugin() {
        return InfractionsPlugin.getInst();
    }
}
