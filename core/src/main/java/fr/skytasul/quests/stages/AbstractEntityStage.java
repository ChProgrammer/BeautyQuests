package fr.skytasul.quests.stages;

import java.util.Map;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import fr.skytasul.quests.QuestsConfiguration;
import fr.skytasul.quests.api.stages.AbstractStage;
import fr.skytasul.quests.api.stages.StageCreation;
import fr.skytasul.quests.editors.TextEditor;
import fr.skytasul.quests.editors.checkers.NumberParser;
import fr.skytasul.quests.gui.ItemUtils;
import fr.skytasul.quests.gui.creation.stages.Line;
import fr.skytasul.quests.gui.mobs.EntityTypeGUI;
import fr.skytasul.quests.players.PlayerAccount;
import fr.skytasul.quests.players.PlayersManager;
import fr.skytasul.quests.structure.QuestBranch;
import fr.skytasul.quests.structure.QuestBranch.Source;
import fr.skytasul.quests.utils.Lang;
import fr.skytasul.quests.utils.MinecraftNames;
import fr.skytasul.quests.utils.Utils;
import fr.skytasul.quests.utils.XMaterial;

public abstract class AbstractEntityStage extends AbstractStage {
	
	protected EntityType entity;
	protected int amount;

	public AbstractEntityStage(QuestBranch branch, EntityType entity, int amount) {
		super(branch);
		this.entity = entity;
		this.amount = amount;
	}
	
	protected void event(Player p, EntityType type) {
		PlayerAccount acc = PlayersManager.getPlayerAccount(p);
		if (branch.hasStageLaunched(acc, this) && canUpdate(p)) {
			if (entity == null || type.equals(entity)) {
				int amount = getPlayerAmount(acc);
				if (amount <= 1) {
					finishStage(p);
				}else {
					updateObjective(acc, p, "amount", --amount);
				}
			}
		}
	}
	
	protected int getPlayerAmount(PlayerAccount acc) {
		return getData(acc, "amount");
	}
	
	@Override
	protected void initPlayerDatas(PlayerAccount acc, Map<String, Object> datas) {
		datas.put("amount", amount);
		super.initPlayerDatas(acc, datas);
	}
	
	@Override
	protected void serialize(Map<String, Object> map) {
		map.put("entityType", entity == null ? "any" : entity.name());
		map.put("amount", amount);
	}
	
	protected String getMobsLeft(PlayerAccount acc) {
		return Utils.getStringFromNameAndAmount(entity == null ? Lang.EntityTypeAny.toString() : MinecraftNames.getEntityName(entity), QuestsConfiguration.getItemAmountColor(), getPlayerAmount(acc), amount, false);
	}
	
	@Override
	protected Object[] descriptionFormat(PlayerAccount acc, Source source) {
		return new Object[] { getMobsLeft(acc) };
	}
	
	public static abstract class AbstractCreator<T extends AbstractEntityStage> extends StageCreation<T> {
		
		protected EntityType entity = null;
		protected int amount = 1;
		
		public AbstractCreator(Line line, boolean ending) {
			super(line, ending);
			
			line.setItem(7, ItemUtils.item(XMaterial.CHICKEN_SPAWN_EGG, Lang.changeEntityType.toString()), (p, item) -> {
				new EntityTypeGUI(x -> {
					setEntity(x);
					reopenGUI(p, true);
				}, this::canUseEntity).create(p);
			});
			
			line.setItem(8, ItemUtils.item(XMaterial.REDSTONE, Lang.Amount.format(1)), (p, item) -> {
				new TextEditor<>(p, () -> {
					reopenGUI(p, false);
				}, x -> {
					setAmount(x);
					reopenGUI(p, false);
				}, NumberParser.INTEGER_PARSER_STRICT_POSITIVE).enter();
			});
		}
		
		public void setEntity(EntityType entity) {
			this.entity = entity;
			line.editItem(7, ItemUtils.lore(line.getItem(7), entity == null ? Lang.EntityTypeAny.toString() : entity.name()));
		}
		
		public void setAmount(int amount) {
			this.amount = amount;
			line.editItem(8, ItemUtils.name(line.getItem(8), Lang.Amount.format(amount)));
		}
		
		@Override
		public void start(Player p) {
			super.start(p);
			setEntity(null);
		}
		
		@Override
		public void edit(T stage) {
			super.edit(stage);
			setEntity(stage.entity);
			setAmount(stage.amount);
		}
		
		protected abstract boolean canUseEntity(EntityType type);
		
	}
	
}