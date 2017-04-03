/**
 * This file is part of Wikiforia.
 *
 * Wikiforia is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Wikiforia is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wikiforia. If not, see <http://www.gnu.org/licenses/>.
 */
 package nlp.wikipedia.lang;

//Autogenerated from Wikimedia sources at 2015-04-16T13:55:11+00:00

public class KwConfig extends TemplateConfig {
	public KwConfig() {
		addNamespaceAlias(-2, "Media");
		addNamespaceAlias(-1, "Arbennek", "Arbednek");
		addNamespaceAlias(1, "Keskows", "Cows", "Kescows");
		addNamespaceAlias(2, "Devnydhyer");
		addNamespaceAlias(3, "Keskows_Devnydhyer", "Cows_Devnydhyer", "Kescows_Devnydhyer");
		addNamespaceAlias(5, "Keskows_Wikipedia", "Cows_Wikipedia", "Kescows_Wikipedia");
		addNamespaceAlias(6, "Restren");
		addNamespaceAlias(7, "Keskows_Restren", "Cows_Restren", "Kescows_Restren");
		addNamespaceAlias(8, "MediaWiki");
		addNamespaceAlias(9, "Keskows_MediaWiki", "Cows_MediaWiki", "Kescows_MediaWiki");
		addNamespaceAlias(10, "Skantlyn", "Scantlyn");
		addNamespaceAlias(11, "Keskows_Skantlyn", "Cows_Scantlyn", "Kescows_Skantlyn");
		addNamespaceAlias(12, "Gweres");
		addNamespaceAlias(13, "Keskows_Gweres", "Cows_Gweres", "Kescows_Gweres");
		addNamespaceAlias(14, "Klass", "Class");
		addNamespaceAlias(15, "Keskows_Klass", "Cows_Class", "Kescows_Class");

		addI18nCIAlias("redirect", "#DASKEDYANS", "#REDIRECT");
		addI18nAlias("numberofpages", "NIVERAFOLENNOW", "NUMBEROFPAGES");
		addI18nAlias("numberofarticles", "NIVERAERTHYGLOW", "NUMBEROFARTICLES");
		addI18nAlias("numberoffiles", "NIVERARESTRENNOW", "NUMBEROFFILES");
		addI18nAlias("numberofusers", "NIVERADHEVNYDHYORYON", "NUMBEROFUSERS");
		addI18nAlias("numberofactiveusers", "NIVERADHEVNYDHYORYONVYW", "NUMBEROFACTIVEUSERS");
		addI18nAlias("numberofedits", "NIVERAJANJYOW", "NUMBEROFEDITS");
		addI18nAlias("numberofviews", "NIVERAWELYANSOW", "NUMBEROFVIEWS");
		addI18nAlias("pagename", "HANOWANFOLEN", "PAGENAME");
		addI18nAlias("fullpagename", "HANOWLEUNANFOLEN", "FULLPAGENAME");
		addI18nAlias("img_thumbnail", "skeusennik", "thumbnail", "thumb");
		addI18nAlias("img_manualthumb", "skeusennik=$1", "thumbnail=$1", "thumb=$1");
		addI18nAlias("img_right", "dyhow", "right");
		addI18nAlias("img_left", "kledh", "left");
		addI18nAlias("img_none", "nagonan", "none");
		addI18nAlias("img_center", "kresel", "center", "centre");
		addI18nAlias("img_framed", "fremys", "framed", "enframed", "frame");
		addI18nAlias("img_frameless", "hebfram", "frameless");
		addI18nAlias("img_page", "folen=$1", "folen_$1", "page=$1", "page $1");
		addI18nAlias("img_top", "gwartha", "top");
		addI18nAlias("img_text_top", "tekst-gwartha", "text-top");
		addI18nAlias("img_middle", "kres", "middle");
		addI18nAlias("img_bottom", "goles", "bottom");
		addI18nAlias("img_text_bottom", "tekst-goles", "text-bottom");
		addI18nAlias("img_link", "kevren=$1", "link=$1");
		addI18nAlias("sitename", "HANOWANWIASVA", "SITENAME");
		addI18nCIAlias("pageid", "IDANFOLEN", "PAGEID");
		addI18nCIAlias("server", "SERVYER", "SERVER");
		addI18nCIAlias("servername", "HANOWANSERVYER", "SERVERNAME");
		addI18nCIAlias("grammar", "GRAMASEK:", "GRAMMAR:");
		addI18nCIAlias("fullurl", "URLLEUN:", "FULLURL:");
		addI18nAlias("displaytitle", "DISKWEDHESANTITEL", "DISPLAYTITLE");
		addI18nCIAlias("language", "#YETH:", "#LANGUAGE:");
		addI18nAlias("numberofadmins", "NIVERAVENYSTRORYON", "NUMBEROFADMINS");
		addI18nCIAlias("special", "arbennek", "special");
		addI18nCIAlias("filepath", "HYNSANFOLEN:", "FILEPATH:");
		addI18nAlias("hiddencat", "__KLASSKUDHYS__", "__HIDDENCAT__");
		addI18nAlias("pagesincategory", "RESTRENNOWYNKLASS", "PAGESINCATEGORY", "PAGESINCAT");
		addI18nAlias("pagesize", "MYNSANRESTREN", "PAGESIZE");
		addI18nAlias("index", "__MENEGVA__", "__INDEX__");
		addI18nAlias("noindex", "__HEBMENEGVA__", "__NOINDEX__");
		addI18nAlias("numberingroup", "NIVERYNBAGAS", "NUMBERINGROUP", "NUMINGROUP");
		addI18nCIAlias("url_path", "HYNS", "PATH");
		addI18nCIAlias("pagesincategory_all", "oll", "all");
		addI18nCIAlias("pagesincategory_pages", "folennow", "pages");
	}

	@Override
	protected String getSiteName() {
		return "Wikipedia";
	}

	@Override
	protected String getWikiUrl() {
		return "http://kw.wikipedia.org/";
	}

	@Override
	public String getIso639() {
		return "kw";
	}
}