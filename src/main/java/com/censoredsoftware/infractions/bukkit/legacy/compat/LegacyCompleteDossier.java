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

import com.censoredsoftware.infractions.bukkit.Infraction;
import com.censoredsoftware.infractions.bukkit.dossier.CompleteDossier;
import com.google.common.collect.Sets;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.Set;
import java.util.UUID;

public class LegacyCompleteDossier extends LegacyDossier implements CompleteDossier
{
	LegacyCompleteDossier()
	{
		super();
	}

	public LegacyCompleteDossier(UUID mojangId, String lastKnownName, Infraction... infractions)
	{
		this(mojangId, lastKnownName, Sets.newHashSet(infractions));
	}

	public LegacyCompleteDossier(UUID mojangId, String lastKnownName, Set<Infraction> infractions)
	{
		super(mojangId, infractions);
		this.lastKnownName = lastKnownName;
	}

	@Override
	public OfflinePlayer getOfflinePlayer()
	{
		return Bukkit.getOfflinePlayer(lastKnownName);
	}

	@Override
	public String getLastKnownName()
	{
		return lastKnownName;
	}

	@Override
	public CompleteDossier complete(String playerName)
	{
		return this;
	}

	@Override
	public CompleteDossier complete()
	{
		return this;
	}
}
