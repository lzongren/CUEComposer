package info.hargrave.composer.ui

import info.hargrave.composer._
import info.hargrave.composer.backend.manager.projects.CUEProject
import info.hargrave.composer.ui.TabbedProjectUI.ProjectTab
import info.hargrave.composer.ui.cue.CUEProjectUI
import info.hargrave.composer.util.Localization

import scala.collection.mutable.{Map => MutableMap}

import info.hargrave.composer.backend.manager.Project
import info.hargrave.composer.backend.manager.ui.ProjectUserInterface

import scalafx.event.EventIncludes
import scalafx.scene.Node
import scalafx.scene.control.TabPane.TabClosingPolicy
import scalafx.scene.control.{Tab, TabPane}

import javafx.scene.{control => jfxsc}

/**
 * Provides a JavaFX frontend for the ProjectController. As such, it allows for the interface to access the selection model
 * in order to determine and set the currently foregrounded project, as well as pass [[Project Projects]] that will be
 * used to create [[ProjectTab ProjectTabs]] with a corresponding user interface element.
 */
class TabbedProjectUI extends TabPane with ProjectUserInterface with Localization {

    import TabbedProjectUI.{ProjectTabAccess, jfxTab2sfx}


    // FX Node configuration --------------------------------------

    tabClosingPolicy = TabClosingPolicy.ALL_TABS

    // Interface configuration ------------------------------------

    private implicit val projectTabs = MutableMap[Project, ProjectTab]()

    override def projects: Iterable[Project] = projectTabs.keys

    @throws(classOf[NoSuchElementException])
    override def closeProject(project: Project): Unit = projectTabs.remove(project) match {
        case tab: Some[ProjectTab] => tabs.remove(tab.get)
        case None => throw new NoSuchElementException(t"error.project.not_open")
    }

    @throws(classOf[IllegalArgumentException])
    override def addProject(project: Project): Unit = project.projectTab match {
        case _:Some[ProjectTab]     => throw new IllegalArgumentException(t"error.project.already_open")
        case None                   =>
            logger.debug(s"Adding $project to tab interface")
            project.projectTab = Some(new ProjectTab(project))
            logger.trace(s"ProjectTab ${project.projectTab.get} corresponds to project")
            tabs.add(project.projectTab.get)
    }

    override def activeProject: Option[Project] = Option(jfxTab2sfx(selectionModel.value.getSelectedItem)) match {
        case pr: Some[Tab]  if pr.get.isInstanceOf[ProjectTab] => Some(pr.get.asInstanceOf[ProjectTab].project)
        case npr: Some[Tab]     =>
            logger.warn(s"The active tab ({$npr) is a not compatible with the project controller")
            None
        case None               => None
    }

    override def switchTo(project: Project): Unit = project.projectTab match {
        case tab: Some[ProjectTab]  =>
            logger.debug(s"switching foregrounded project to ${tab.get}")
            selectionModel.get.select(tab.get)
        case None                   => throw new NoSuchElementException(t"error.project.not_open")
    }
}
object TabbedProjectUI {

    /**
     * Implicit decorator that provides access to a ProjectTab given a project and a map of Project->ProjectTab implicitly
     * available in the conversion scope.
     *
     * @param project project
     * @param componentAccess map of project/projecttab
     */
    implicit class ProjectTabAccess (project: Project)(implicit val componentAccess: MutableMap[Project, ProjectTab]) {

        def projectTab_=(tab: Option[ProjectTab]): Option[ProjectTab] = tab match {
            case someTab: Some[ProjectTab]  => componentAccess.put(project, someTab.get)
            case None                       =>
                logger.warn("Caller accessed projectTab setter with the intent to remove a ProjectTab via the implicit decorator!")
                componentAccess.remove(project)
                None
        }

        def projectTab: Option[ProjectTab] = componentAccess.get(project)
    }

    /**
     * Tab implementation that accepts a project and constructs an appropriate user interface component (if one is present)
     * from the function provided by [[InterfaceComponentAssociations]].
     *
     * @param project project
     */
    final class ProjectTab(val project: Project) extends Tab with EventIncludes {

        delegate.getProperties.put(classOf[ProjectTab], this)

        val projectInterfaceComponent = InterfaceComponentAssociations(project.getClass)(project)

        content = projectInterfaceComponent
        text    = project.title

        override def toString(): String = s"ProjectTab(delegate=$delegate, project=$project)"

        override def userData_=(x: AnyRef): Unit = {}
    }


    implicit def jfxTab2sfx(jfx: jfxsc.Tab): Tab = {
        if(jfx != null) Option(jfx.getProperties.get(classOf[ProjectTab])) match {
            case someData: Some[AnyRef] if someData.get.isInstanceOf[ProjectTab] => someData.get.asInstanceOf[ProjectTab]
            case _ =>
                new Tab(jfx)
        } else null
    }


    /**
     * Provides factory-like associations that allow the lookup of a function to construct a project interface object if
     * provided with a project.
     */
    val InterfaceComponentAssociations  = Map[Class[_<:Project], ((Project)=>Node)](classOf[CUEProject] -> ((p: Project) => new CUEProjectUI(p.asInstanceOf[CUEProject])))

}

