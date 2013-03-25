#!/usr/bin/python

# Author: Ben Wing <ben@666.com>
# Date: April 2006

#############################################################################
#                                                                           #
#                                ccg_editor.ply                             #
#                                                                           #
#   Edit a CCG-format file, graphically.  Will have a mode for displaying   #
#   CCG files in a friendly fashion and allowing for editing of parts or    #
#   all of the file.  Will also have a mode for testing a CCG grammar, and  #
#   allow for compilation and error-finding under control of the editor.    #
#                                                                           #
#############################################################################

# This code is based on PyEdit version 1.1, from Oreilly's Programming
# Python, 2nd Edition, 2001, by Mark Lutz.

from Tkinter        import *               # base widgets, constants
from tkFileDialog   import *               # standard dialogs
from tkMessageBox   import *
from tkSimpleDialog import *
from tkColorChooser import askcolor
from ScrolledText import ScrolledText
from string         import split, atoi
import sys, os, string, md5
import ccg2xml
import Tree
import re

START     = '1.0'                          # index of first char: row=1,col=0
SEL_FIRST = SEL + '.first'                 # map sel tag to index
SEL_LAST  = SEL + '.last'                  # same as 'sel.last'

FontScale = 0                              # use bigger font on linux
if sys.platform[:3] != 'win':              # and other non-windows boxes
    FontScale = 3

# Initial top-level window; it's not clear we need this.
# FIXME: It sucks that we have to call Tk() to get the first top-level window
# but Toplevel() for all others.  We should be able to call Tk() initially,
# and then Toplevel() to create all top-level windows, including the first.
root = None

# List of all open CFile objects
openfiles = {}
filenames = []

class CTab(Frame):
    # Initialize this tab.  Usually called from a subclass.  PARENT is
    # the parent widget, CFILE the CFile object associated with the
    # top-level window, and TABNAME is the name of this tab (that tab
    # will be removed from the toolbar).
    def __init__(self, parent, cfile, tabname):
        Frame.__init__(self, parent)
        self.parent = parent
        self.cfile = cfile
        self.toolbar = None
        self.checkbar = None
        self.menubar = [
            ('File', 0, 
                 [('Open...',    0, self.cfile.onOpen),
                  ('New',        0, self.cfile.onNew),
                  ('Save',       0, self.onSave),
                  ('Save As...', 5, self.onSaveAs),
                  ('Close',      0, self.cfile.onClose),
                  'separator',
                  ('Quit VisCCG',    0, self.cfile.onQuit)]
            ),
            ('Tools', 0,
                 [('Font List',   0, self.cfile.onFontList),
                  ('Pick Bg...',  4, self.cfile.onPickBg),
                  ('Pick Fg...',  0, self.cfile.onPickFg),
                  ('Color List',  0, self.cfile.onColorList),
                 'separator',
                  ('Info...',    0, self.cfile.onInfo),
                  ]
            )]
        self.toolbar = [
            #('Display',   self.cfile.onDisplay,    {'side': LEFT}),
            ('Edit',   self.cfile.onEdit,       {'side': LEFT}),
            ('Lexicon',   self.cfile.onLexicon,  {'side': LEFT}),
            ('Testbed',   self.cfile.onTestbed,  {'side': LEFT}),
            ('Features',   self.cfile.onFeatures,  {'side': LEFT}),
            ('Words',   self.cfile.onWords,  {'side': LEFT}),
            ('Rules',   self.cfile.onRules,  {'side': LEFT}),
            ('Quit',  self.cfile.onClose,   {'side': RIGHT}),
            ('Help',  self.cfile.help,     {'side': RIGHT}),
            ('Save',  self.onSave,         {'side': RIGHT}),
            ]
#        self.remove_toolbar_button(tabname)

    # Add MENU (a tuple corresponding to a single top-level menu item)
    # after the item with the name AFTER.
    def add_menu(self, after, menu):
        newmenu = []
        for x in self.menubar:
            newmenu += [x]
            if x[0] == after:
                newmenu += [menu]
        self.menubar = newmenu

    # Remove the toolbar button named NAME.
    def remove_toolbar_button(self, name):
        newtoolbar = []
        for x in self.toolbar:
            if x[0] != name:
                newtoolbar += [x]
        self.toolbar = newtoolbar

    def reinit(self):
        pass

    #####################
    # File menu commands
    #####################

    def onSave(self):
        self.onSaveAs(self.cfile.currfile)  # may be None

    def onSaveAs(self, forcefile=None):
        file = forcefile or self.cfile.my_asksaveasfilename()
        if file:
            text = self.cfile.getAllText()
            try:
                open(file, 'w').write(text)
            except:
                showerror('CCG Editor', 'Could not write file ' + file)
            else:
                self.cfile.setFileName(file)         # may be newly created
                self.cfile.edit_modified(NO)
		self.cfile.last_save_signature = self.cfile.getSignature(text)


class CEdit(CTab):
    def __init__(self, parent, cfile):
        CTab.__init__(self, parent, cfile, 'Edit')
	self.debugFrame= None

	# Add a frame here, so that debug mode can be enabled
	# by embedding other objects within this frame
	editFrame = Frame(self, bd=1, bg= 'white')
	editFrame.pack(fill=BOTH, expand=YES, side=TOP)

	# Add a button frame, embed the button and 
	# link to command for the debug mode
	btnFrame = Frame(editFrame, bd = 1)
	btnFrame.grid (row=0, columnspan=3, sticky=NSEW)
	
	vldButton = Button (btnFrame, text='Validate', command = lambda: self.onValidate(editFrame, cfile))
	vldButton.pack(side=RIGHT)

	# Put the main edit window in the row below this
        vbar  = Scrollbar(editFrame)  
        hbar  = Scrollbar(editFrame, orient='horizontal')
        self.text  = Text(editFrame, padx=5, wrap='none', undo=YES)

        vbar.grid(row=1, column=2, sticky=NS)
        hbar.grid(row=2, columnspan=2, sticky=EW)     # pack text last
        self.text.grid(row=1, column=1, sticky=NSEW)  # else sbars clipped

	editFrame.columnconfigure(1, weight=1)
	editFrame.rowconfigure(1, weight=1)

	# Add a list containing line numbers
	self.lineList = Text(editFrame, relief=SUNKEN, bg='white', bd=2, yscrollcommand = vbar.set, width=3)
	self.lineList.grid(row=1, column=0, sticky=NS)
        self.lineList.config(font=self.cfile.fonts[0],
                    bg=self.cfile.colors[0]['bg'], fg=self.cfile.colors[0]['fg'])
		    

	# TODO: The first time the display of the line numbers
	# strangely doesn't go through --- somehow cfile
	# isn't initialized. However, it works properly in the display.
	# Need to understand why this happens.

	try:
		self.showLineNums()
	except KeyError: 
		dummy =1
		
        self.text.config(yscrollcommand=vbar.set)    # call vbar.set on text move
        self.text.config(xscrollcommand=hbar.set)
        #vbar.config(command=text.yview)         # call text.yview on scroll move
        hbar.config(command=self.text.xview)         # or hbar['command']=text.xview

        self.text.config(font=self.cfile.fonts[0],
                    bg=self.cfile.colors[0]['bg'], fg=self.cfile.colors[0]['fg'])


	#Setting the movement of the listbox and the text
	#together to be controlled by the scrollbar
	vbar.config(command=self.scrollSet)

        self.add_menu('File',
                      ('Edit', 0,
                       [('Cut',        0, self.onCut),
                        ('Copy',       1, self.onCopy),
                        ('Paste',      0, self.onPaste),
                        'separator',
                        ('Delete',     0, self.onDelete),
                        ('Select All', 0, self.onSelectAll)]
                       ))
        self.add_menu('Edit',
                      ('Search', 0,
                       [('Goto...',    0, self.cfile.onGoto),
                        ('Find...',    0, self.cfile.onFind),
                        ('Refind',     0, self.cfile.onRefind),
                        ('Change...',  0, self.onChange)]
                       ))
	#print "Will exit the init function now"

    def scrollSet(self, *args):
    	self.lineList.yview(*args)
	self.text.yview(*args)

    def reinit(self):
    	self.showLineNums()
        self.text.focus()

    def showLineNums(self):
    	#Make the list of lines editable
	self.lineList.config(state=NORMAL)
	textData = self.cfile.getAllText()
	listOfLines = textData.split('\n')
	for num in range(1,len(listOfLines)):
		self.lineList.insert(END,"%s\n" % num)
	#Now that we are done changing the number of lines, 
	#we reset the text to be uneditable
	self.lineList.config(state=NORMAL)

    def onValidate(self, editFrame, cfile):
	#showwarning(title= 'Sorry', message='Validate and debug feature coming soon!')
	# Destroy previous display of debug or error messages
	# if present
	if self.debugFrame:
		self.debugFrame.grid_forget()

	# Compile if file signature has changed
	cfile.compile_if_needed()

	# Now, call the error debug routine if errors are found
	if (ccg2xml.error_count > 0):
		self.debugError(editFrame, cfile)
	else:
		showinfo(title='VisCCG: Success', message='No validation errors!')

    def debugError(self, editFrame, cfile):
    	self.debugFrame = Frame(editFrame, bg='white', bd=2)
	self.debugFrame.grid(row=3, columnspan=2, sticky=NSEW)

	# Create Listbox and scrollbars
	sbar = Scrollbar(self.debugFrame)
	list = Listbox(self.debugFrame, relief=SUNKEN, bg='white', bd=2, yscrollcommand = sbar.set)
	sbar.config(command=list.yview)
	list.pack(fill=BOTH, side=LEFT, expand=YES)
	sbar.pack(fill=Y, side=RIGHT)

	# Display each message in the log
        for mesg in ccg2xml.message_log:
		type = mesg[0]
		lineno = mesg[1]
		errwarn = mesg[2]

		if lineno:
			dispError = type+' at Line '+str(lineno)+': '+errwarn
		else:
			dispError = type+': '+errwarn

		list.insert(END, dispError)

    #####################
    # Edit menu commands
    #####################

    def onCopy(self):                           # get text selected by mouse,etc
        if not self.text.tag_ranges(SEL):       # save in cross-app clipboard
            showerror('CCG Editor', 'No text selected')
        else:
            text = self.text.get(SEL_FIRST, SEL_LAST)  
            self.clipboard_clear()              
            self.clipboard_append(text)

    def onDelete(self):                         # delete selected text, no save
        if not self.text.tag_ranges(SEL):
            showerror('CCG Editor', 'No text selected')
        else:
            self.text.delete(SEL_FIRST, SEL_LAST)

    def onCut(self):
        if not self.text.tag_ranges(SEL):
            showerror('CCG Editor', 'No text selected')
        else: 
            self.onCopy()                       # save and delete selected text
            self.onDelete()

    def onPaste(self):
        try:
            text = self.selection_get(selection='CLIPBOARD')
        except TclError:
            showerror('CCG Editor', 'Nothing to paste')
            return
        self.text.insert(INSERT, text)          # add at current insert cursor
        self.text.tag_remove(SEL, '1.0', END) 
        self.text.tag_add(SEL, INSERT+'-%dc' % len(text), INSERT)
        self.text.see(INSERT)                   # select it, so it can be cut

    def onSelectAll(self):
        self.text.tag_add(SEL, '1.0', END+'-1c')   # select entire text 
        self.text.mark_set(INSERT, '1.0')          # move insert point to top
        self.text.see(INSERT)                      # scroll to top

    #######################
    # Search menu commands
    #######################
 
    def onChange(self):
        new = Toplevel(self)
        Label(new, text='Find text:').grid(row=0, column=0)
        Label(new, text='Change to:').grid(row=1, column=0)
        self.change1 = Entry(new)
        self.change2 = Entry(new)
        self.change1.grid(row=0, column=1, sticky=EW)
        self.change2.grid(row=1, column=1, sticky=EW)
        Button(new, text='Find',  
               command=self.onDoFind).grid(row=0, column=2, sticky=EW)
        Button(new, text='Apply', 
               command=self.onDoChange).grid(row=1, column=2, sticky=EW)
        new.columnconfigure(1, weight=1)    # expandable entrys

    def onDoFind(self):
        self.onFind(self.change1.get())                    # Find in change box

    def onDoChange(self):
        if self.text.tag_ranges(SEL):                      # must find first
            self.text.delete(SEL_FIRST, SEL_LAST)          # Apply in change
            self.text.insert(INSERT, self.change2.get())   # deletes if empty
            self.text.see(INSERT)
            self.onFind(self.change1.get())                # goto next appear
            self.text.update()                             # force refresh

    ####################################
    # Others, useful outside this class
    ####################################

    def isEmpty(self):
        return not self.getAllText() 

    def getAllText(self):
        return self.text.get('1.0', END+'-1c')  # extract text as a string

    def setAllText(self, text):
        self.text.delete('1.0', END)            # store text string in widget
        self.text.insert(END, text)             # or '1.0'
        self.text.mark_set(INSERT, '1.0')       # move insert point to top 
        self.text.see(INSERT)                   # scroll to top, insert set
        self.cfile.edit_modified(NO)

    def clearAllText(self):
        self.text.delete('1.0', END)            # clear text in widget 

# class CDisplay(CTab):
#     def __init__(self, parent, cfile):
#         CTab.__init__(self, parent, cfile, 'Display')

#         # Use built-in text-with-scrollbar widget
#         text = ScrolledText(self)
#         text.config(font=self.cfile.fonts[0], 
#                     bg=self.cfile.colors[0]['bg'], fg=self.cfile.colors[0]['fg'])
#         #text.config(font=('courier', 10, 'normal'))  # use fixed-width font
#         text.pack(side=TOP, fill=BOTH, expand=YES)

#         text.config(font=self.cfile.fonts[0], 
#                     bg=self.cfile.colors[0]['bg'], fg=self.cfile.colors[0]['fg'])
#         self.text = text
#
#         self.add_menu('Edit',
#                       ('Search', 0,
#                        [('Goto...',    0, self.cfile.onGoto),
#                         ('Find...',    0, self.cfile.onFind),
#                         ('Refind',     0, self.cfile.onRefind),
#                        ))
#
#     def setAllText(self, text):
#         self.text.config(state=NORMAL)
#         self.text.delete('1.0', END)            # store text string in widget
#         self.text.insert(END, text)             # or '1.0'
#         self.text.mark_set(INSERT, '1.0')       # move insert point to top 
#         self.text.see(INSERT)                   # scroll to top, insert set
#         self.text.config(state=DISABLED)

#     def reinit(self):
#         self.setAllText(self.cfile.getAllText())
#         self.text.focus()

class CWords(CTab):
    def __init__(self, parent, cfile):
        CTab.__init__(self, parent, cfile, 'Words')
	self.child=None
	self.wordList = None
	self.cfile = cfile

    # Called when we switch to this mode using the toolbar at top.
    def reinit(self):
        if self.child:
            self.child.pack_forget()

        self.child = Frame(self, background='white')
        self.child.pack(expand=YES, fill=BOTH)

	scrollbar = Scrollbar(self.child, orient=VERTICAL)
	self.wordList = Listbox(self.child, yscrollcommand=scrollbar.set)
	self.wordList.grid(row=0, column=0, sticky=N+S+E+W)

	scrollbar.config(command= self.wordList.yview)
	scrollbar.grid(row=0, column=1, sticky=N+S)

	self.child.grid_rowconfigure(0, weight=1)
	self.child.grid_columnconfigure(0, weight=1)

	#If the data hasn't been compiled yet, then do so
	try: 
		dummy = ccg2xml.morph_xml
	except:
		self.cfile.compile_if_needed()
	#Adding dummy code for all words
	for x in ccg2xml.morph_xml:
		assert x[0] == 'entry'
		self.wordList.insert (END, ccg2xml.getprop('word', x[1]))
		#print ccg2xml.getprop('word', x[1])

class CLexicon(CTab):
    class lexicon_vars(object):
        def __init__(self):
            self.show_feat_id = IntVar()
            self.show_feat_id.set(1)
            self.show_feat_struct = IntVar()
            self.show_feat_struct.set(1)
            self.show_full_features = IntVar()
            self.show_full_features.set(0)
            self.show_semantics = IntVar()
            self.show_semantics.set(1)

    def __init__(self, parent, cfile):
        CTab.__init__(self, parent, cfile, 'Lexicon')
        self.child = None
	self.cnv = None
	self.mainFrame = None

        self.vars = self.lexicon_vars()
        # FIXME?  It's a bit awkward that ccg.ply has references to the
        # variables below scattered throughout it.  But I'm not sure what
        # a better solution would be.
        self.checkbar = [
            ("Show feature ID's", self.vars.show_feat_id),
            ("Show features", self.vars.show_feat_struct),
            ('Full-form features', self.vars.show_full_features),
            ('Show semantics', self.vars.show_semantics),
            ]

    # Called when we switch to this mode using the toolbar at top.
    def reinit(self):
        self.redraw()

    # Called when a change is made to a checkbox setting.
    # FIXME: There may be a smarter way to do this.
#    def redraw(self):
#        self.cfile.compile_if_needed()
#        if self.child:
#	    #self.cnv.pack_forget()
#            self.child.pack_forget()
#        
#	self.child = Frame(self, bd=2, relief=SUNKEN, background='white')
#
#	self.child.pack(expand=YES, fill=BOTH)
#	
#	self.child.rowconfigure(1, weight=1)
#	self.child.columnconfigure(1, weight=1)
#
#        ccg2xml.draw_parse(self.cfile.curparse.parse, self.cfile, self.child,
#                           self.vars)

    def redraw(self):
    	self.cfile.compile_if_needed()
	if self.child:
		self.child.pack_forget()
	if self.mainFrame:
		self.mainFrame.pack_forget()

	self.mainFrame = Frame(self, bd=1, bg='white')	
	self.mainFrame.pack_propagate(0)
	self.mainFrame.pack(expand=YES, fill=BOTH)

	self.mainFrame.grid_rowconfigure(0, weight=1)
	self.mainFrame.grid_columnconfigure(0, weight=1)

	xscrollbar = Scrollbar(self.mainFrame, orient=HORIZONTAL)
	xscrollbar.grid(row=1, column=0, sticky=E+W)

	yscrollbar = Scrollbar(self.mainFrame)
	yscrollbar.grid(row=0, column=1, sticky=N+S)

	self.cnv= Canvas(self.mainFrame, bd=2, xscrollcommand=xscrollbar.set, 
		yscrollcommand=yscrollbar.set, width = 847, height=369)
		#height= self.cfile.top.cget('height'),
		#width = self.cfile.top.cget('width'))

	#print self.cfile.top.cget('height')
	#print self.cfile.top.cget('width')

	xscrollbar.config(command= self.cnv.xview)
	yscrollbar.config(command= self.cnv.yview)

	self.child = Frame(self.cnv, bd=2, relief=SUNKEN, background='white')
	#self.child.grid_rowconfigure(0, weight=1)
	#self.child.grid_columnconfigure(0, weight=1)

	self.cnv.create_window(0, 0, anchor='nw', window=self.child)

	ccg2xml.draw_parse(self.cfile.curparse.parse, self.cfile, self.child, self.vars, self.cnv, self.mainFrame)

	self.child.update_idletasks()
	#self.child.grid(row=0, column=0, sticky=NSEW)

	self.cnv.config(scrollregion=self.cnv.bbox("all"))
	self.cnv.grid(row=0, column=0, sticky='NSEW')


class CRules(CTab):
    def __init__(self, parent, cfile):
        CTab.__init__(self, parent, cfile, 'Rules')

class CFeatures(CTab):
    def __init__(self, parent, cfile):
        CTab.__init__(self, parent, cfile, 'Features')
	self.child=None
	self.checkbar=None
	self.edit=None
	self.text=None

    # Called when we switch to this mode using the toolbar at top.
    def reinit(self):
        if self.child:
            self.child.pack_forget()

        self.child = Frame(self, background='white', width = 847, height = 369)
        self.child.pack(expand=YES, fill=BOTH)
        butframe = Frame(self.child, cursor='hand2',
                         relief=SUNKEN, bd=2)
        butframe.pack(fill=X)
        but1 = Button(butframe, text='Expand All', command=self.expand_all)
        but1.pack(side=LEFT)
        but2 = Button(butframe, text='Contract All', command=self.contract_all)
        but2.pack(side=LEFT)
	# Force editing in the same frame: but a lower view: 
	# pass self.child as the parent frame
        self.edit = Button(butframe, text='Edit', command= lambda:self.edit_tree(self.child))
        self.edit.pack(side=RIGHT)
        featframe = Frame(self.child, bd=2, relief=SUNKEN,
                          background='white')
        featframe.pack(expand=YES, fill=BOTH)
        self.cfile.compile_if_needed()

	# Build the tree
	self.tree={}
	self.root_name = re.sub(r'^(.*)\.(.*)$', r'\1', self.cfile.file)
	self.tree[self.root_name]=[]
	for feat in self.cfile.curparse.feature_to_values:
		self.tree[self.root_name] += [str(feat)]
	for feat in self.cfile.curparse.feature_to_values:
		self.tree[feat] = []
		
		for x in self.cfile.curparse.feature_to_values[feat]:
			if x.name not in self.tree :
				self.tree[x.name] = []

		for x in self.cfile.curparse.feature_to_values[feat]:
			if x.parents:
				par = x.parents[0]
				self.tree[par.name] += [x.name]
			else:
				self.tree[feat] += [x.name]
	
	# Define the images for opened and closed categories
	shut_icon=PhotoImage(data='R0lGODlhCQAQAJH/AMDAwAAAAGnD/wAAACH5BAEAAAAALAAA'
                     'AAAJABAAQAIdhI8hu2EqXIroyQrb\nyRf0VG0UxnSZ5jFjulrhaxQ'
                     'AO6olVwAAOw==')
	open_icon=PhotoImage(data='R0lGODlhEAAJAJH/AMDAwAAAAGnD/wAAACH5BAEAAAAALAAA'
                     'AAAQAAkAQAIahI+pyyEPg3KwPrko\nTqH7/yGUJWxcZTapUQAAO8b'
                     'yUgAAOw==')

	# Create the tree
	self.t=Tree.Tree(master=featframe,
		    root_id='',
		    root_label=self.root_name,
		    collapsed_icon = shut_icon,
		    expanded_icon = open_icon,
		    get_contents_callback = self.get_treedata,
		    line_flag=False)
	
	self.t.grid(row=0, column=0, sticky = 'nsew')

	featframe.grid_rowconfigure(0, weight=1)
	featframe.grid_columnconfigure(0, weight=1)

	sb=Scrollbar(featframe)
	sb.grid(row=0, column=1, sticky='ns')
	self.t.configure(yscrollcommand=sb.set)
	sb.configure(command=self.t.yview)

	sb=Scrollbar(featframe, orient=HORIZONTAL)
	sb.grid(row=1, column=0, sticky='ew')
	self.t.configure(xscrollcommand=sb.set)
	sb.configure(command=self.t.xview)

	# Expand the whole tree out
	self.expand_tree(self.t.root)

    # Returns the nodes rooted at the node passed and adds them to the tree
    def get_treedata(self,node):
    	lbl = str(node.get_label())
	children = self.tree[lbl]
	for x in children:
		if self.tree[x]:
			expands=1
		else:
			expands=0
		self.t.add_node(name=x,flag=expands)

    # Expand the tree rooted at node recursively
    def expand_tree(self, node):
    	node.expand()
	for child in node.children():
		if child.expandable():
			self.expand_tree(child)

    def expand_all(self):
        self.expand_tree(self.t.root)

    def contract_all(self):
        self.t.root.collapse()

    def edit_tree(self, parent):
        editFrame = Frame(parent, bd=1, background='white')


        self.text = Text(editFrame, padx=5, wrap=None, undo = YES, background='white')
        vbar = Scrollbar(editFrame)
        hbar = Scrollbar(editFrame, orient='horizontal')

        self.text.config(yscrollcommand=vbar.set)    # call vbar.set on text move
        self.text.config(xscrollcommand=hbar.set)
        vbar.config(command=self.text.yview)         # call text.yview on scroll move
        hbar.config(command=self.text.xview)         # or hbar['command']=text.xview

        # Change the text on the button, and also pass the rest
        # of the arguments so that the grid for the statements can be reset
        self.edit.config(text='Done', command= lambda:self.save_tree(parent))

        # Changing the mode of the cfile object here,
        # so that once the user clicks done,
        # the whole object is recompiled and redisplayed
        self.cfile.mode= 'Edit'

        vbar.pack(side=RIGHT, fill=Y)
        hbar.pack(side=BOTTOM, fill=X)
        self.text.pack(fill= BOTH, expand= YES)

        # Set a mark at the beginning of the text
        self.text.mark_set("START", INSERT)
        self.text.mark_gravity("START", LEFT)

        # Push in the rest of the file's contents
        fileData = self.cfile.getAllText()
        self.text.insert(INSERT, fileData)

        # Move the insert position to the first occurence of the family name
        # FIXME: this is poor implementation
        # The positioning of the insert cursor should be happening by parsing the
        # CFG production rules, using CSFamily.prod.lineno and endlineno
        self.text.config(takefocus=True)
        idx= self.text.search('feature', "START")
	if idx:
		self.text.mark_set(CURRENT, idx)
        	self.text.see(CURRENT)
	else:
		showwarning('Warning','Features not located in text')

        editFrame.pack(expand=YES, fill=BOTH)

    def save_tree(self, parent):
        # We force the text contents of the cfile object to copy over
        # all that is presently in the current text-box
        self.cfile.setAllText(self.text.get(1.0,END))
        self.edit.config(text='Edit', command= lambda:self.edit_tree(parent))

        # Recompile whatever was edited and redisplay
        # Note: changes are not saved hereby!!
        self.cfile.compile_if_needed()
        self.cfile.onFeatures()
    	
class CTestbed(CTab):
    def __init__(self, parent, cfile):
        CTab.__init__(self, parent, cfile, 'Testbed')
        self.child = None
	self.edit = None
	self.text = None
	self.editFrame = None
	self.cnv = None
	self.mainFrame = None
	self.newInsert = None

    def makelab(self, text, row, col, **props):
        lab = Label(self.child, text=text, background='white', **props)
        # Make the label grow to fill all space allocated for the column
        lab.grid(row=row, column=col, sticky='NSEW')

    # Called when we switch to this mode using the toolbar at top.
    def reinit(self):
        if self.child:
            self.child.pack_forget()
	if self.mainFrame:
	    self.mainFrame.pack_forget()

	self.mainFrame = Frame(self, bd=1, bg='white')
	self.mainFrame.pack(expand=YES, fill=BOTH)

	self.mainFrame.grid_rowconfigure(0, weight=1)
	self.mainFrame.grid_columnconfigure(0, weight=1)

	xscrollbar = Scrollbar(self.mainFrame, orient=HORIZONTAL)
	xscrollbar.grid(row=1, column=0, sticky=E+W)

	yscrollbar = Scrollbar(self.mainFrame)
	yscrollbar.grid(row=0, column=1, sticky=N+S)

	self.cnv= Canvas(self.mainFrame, bd=2, xscrollcommand=xscrollbar.set, 
		yscrollcommand=yscrollbar.set, width = 847, height=369)

	xscrollbar.config(command=self.cnv.xview)
	yscrollbar.config(command=self.cnv.yview)

	self.child = Frame(self.cnv, bd=2, relief=SUNKEN, background='white')


	self.child.rowconfigure(1, weight=1)
	self.child.columnconfigure(1, weight=1)
        self.child.pack(expand=YES, fill=BOTH)

	butnFrame = Frame(self.child, relief=SUNKEN, bd=2)
	butnFrame.grid(row=0, sticky='NSEW', columnspan=2)

	self.edit = Button(butnFrame, text='Edit', command= self.edit_testbed)
	self.edit.pack(side=RIGHT)
	self.newInsert = Button(butnFrame, text='New Sentence', command= self.new_sentence)
	self.newInsert.pack(side=RIGHT)

        self.cfile.compile_if_needed()

	self.makelab("Num Parses", 1, 0, bd=1, relief=SUNKEN, fg="#77AA77", font = ("Helvetica", FontScale +12))
        self.makelab("Sentence", 1, 1, bd=1, relief=SUNKEN, fg="#77AA77", font = ("Helvetica", FontScale +12))

        # Make the column containing the sentences grow to include all
        # extra space
        self.child.columnconfigure(1, weight=1)
        for i in xrange(len(self.cfile.curparse.testbed_statements)):
            x = self.cfile.curparse.testbed_statements[i]
            assert x[0] == 'item'
            x = x[1]
            # Left-justify the text
            numparse = ccg2xml.getprop('numOfParses', x)
            string = ccg2xml.getprop('string', x)

	    # How many parses of the sentence are produced?
            self.makelab('%s' % numparse, i+2, 0)

	    # Print the sentence itself
            self.makelab('%s%s' % (numparse == 0 and '*' or '', string),
                         i+2, 1, anchor=W)

	self.cnv.create_window(0, 0, anchor='nw', window=self.child)

	self.child.update_idletasks()
	#self.child.grid(row=0, column=0, sticky=NSEW)

	self.cnv.config(scrollregion=self.cnv.bbox("all"))
	self.cnv.grid(row=0, column=0, sticky='NSEW')


    # Edit the testbed
    def edit_testbed(self):
        self.editFrame = Frame(self.mainFrame, bd=1, background='white')
	#self.editFrame.grid(row=len(self.cfile.curparse.testbed_statements)+3, columnspan=2, sticky='NSEW')
	self.editFrame.grid(row=2, columnspan=2, sticky='NSEW')

        self.text = Text(self.editFrame, padx=5, wrap=None, undo = YES, background='white')
        vbar = Scrollbar(self.editFrame)
        hbar = Scrollbar(self.editFrame, orient='horizontal')

        self.text.config(yscrollcommand=vbar.set)    # call vbar.set on text move
        self.text.config(xscrollcommand=hbar.set)
        vbar.config(command=self.text.yview)         # call text.yview on scroll move
        hbar.config(command=self.text.xview)         # or hbar['command']=text.xview

        # Change the text on the button, and also pass the rest
        # of the arguments so that the grid for the statements can be reset
        self.edit.config(text='Done', command= self.save_testbed)

	# Changing the mode of the cfile object here,
        # so that once the user clicks done,
        # the whole object is recompiled and redisplayed
        self.cfile.mode= 'Edit'

        vbar.pack(side=RIGHT, fill=Y)
        hbar.pack(side=BOTTOM, fill=X)
        self.text.pack(fill= BOTH, expand= YES)

        # Set a mark at the beginning of the text
        self.text.mark_set("START", INSERT)
        self.text.mark_gravity("START", LEFT)

        # Push in the rest of the file's contents
        fileData = self.cfile.getAllText()
        self.text.insert(INSERT, fileData)

        # Move the insert position to the first occurence of the family name
        # FIXME: this is poor implementation
        # The positioning of the insert cursor should be happening by parsing the
        # CFG production rules, using CSFamily.prod.lineno and endlineno
        self.text.config(takefocus=True)
        idx= self.text.search('testbed', "START")
	if idx:
		self.text.mark_set(CURRENT, idx)
	        self.text.see(CURRENT)
	else:
		showwarning(title= 'VisCCG: Warning', message='No initial testbed found')

	#self.editFrame.pack(expand=YES, fill=BOTH)
	self.child.update_idletasks()
	self.cnv.config(scrollregion=self.cnv.bbox("all"))

    # Save the edited text
    def save_testbed(self):
        # We force the text contents of the cfile object to copy over
        # all that is presently in the current text-box
        self.cfile.setAllText(self.text.get(1.0,END))
        self.edit.config(text='Edit', command= self.edit_testbed)
	self.editFrame.pack_forget()

        # Recompile whatever was edited and redisplay
        # Note: changes are not saved hereby!!
        self.cfile.compile_if_needed()
        self.cfile.onTestbed()

    # Enter a new sentence
    def new_sentence(self):
    	master = Tk()
	master.title('VisCCG: New Sentence for the testbed') 
    	sent = Entry(master, bg='#FFFFFF', width = 100)
	nParses = Entry(master, bg='#FFFFFF', width = 2)

	sLabel = Label (master, text = 'Sentence:')
	nLabel = Label (master, text = 'Number of parses:')

	sent.focus_set()
	
	b = Button(master, text="Add sentence", width=10, command= lambda:self.editNew(master, sent, nParses))
	c = Button(master, text="Cancel", command= master.destroy)

	sent.grid (row=1, column=0, sticky = W)
	nParses.grid (row=1, column=1, sticky= W)
	sLabel.grid (row=0, column=0, sticky=W)
	nLabel.grid (row=0, column=1, sticky = W)
	b.grid (row=2, column = 0)
	c.grid (row=2, column = 1)

    # Print from the new sentence
    def editNew(self, master, sent, nParses):
        # Prepare the file's contents for editing
        fileData = self.cfile.getAllText()
	self.text = Text(master)

        self.text.mark_set("START", INSERT)
        self.text.mark_gravity("START", LEFT)
        self.text.insert(INSERT, fileData)

	testSent = sent.get()
	npSent = nParses.get()

        self.text.config(takefocus=True)
        idx= self.text.search('testbed', "START")
	if idx:
		self.text.mark_set("START", idx)
        	idx= self.text.search('{', "START", forwards = True)
		self.text.mark_set("START", idx)
        	idx= self.text.search('\n', "START", forwards = True)
		# FIXME: really poor search for locating the right position 
		# to insert text here. Needs correction!
		self.text.mark_set(INSERT, idx)
        	self.text.mark_gravity(INSERT, RIGHT)

		self.text.insert (INSERT, '\n\t'+ testSent+ ':\t'+  npSent+ ';')

	else:
		showwarning(title= 'VisCCG: Warning', message='No initial testbed found, creating new')
		self.text.mark_set(INSERT, END)
		self.text.mark_gravity(INSERT, RIGHT)

		self.text.insert (INSERT, ' testbed {\n')
		self.text.insert (INSERT, '\n\t'+ testSent+ ':\t'+  npSent+ ';')
		self.text.insert (INSERT, '}\n')

	
	# Set the original file's data to be this
	fileData= self.text.get(1.0, END)
	self.cfile.setAllText(fileData)

	# Destroy the entry window
	master.destroy()

	# Update the display
        self.cfile.mode= 'Edit'
        self.cfile.compile_if_needed()
        self.cfile.onTestbed()

# Creates the top-level window and populates the widgets below it.
class CFile(object):
    #### NOTE NOTE NOTE! Variables declared like this, in the class itself,
    #### are class variables (not instance variables) until they are
    #### assigned to.  If you want pure instance variables, you need to
    #### initialize them inside of __init__().

    # Hash table describing modes and the associated class
    modelist = {'Edit':CEdit, 'Lexicon':CLexicon, 'Features':CFeatures,
                'Words':CWords, 'Testbed':CTestbed, 'Rules':CRules}

    startfiledir = '.'
    ftypes = [('All files',     '*'),                 # for file open dialog
              ('Text files',   '.txt'),               # customize in subclass
              ('Python files', '.py')]                # or set in each instance

    colors = [{'fg':'black',      'bg':'white'},      # color pick list
              {'fg':'yellow',     'bg':'black'},      # first item is default
              {'fg':'white',      'bg':'blue'},       # tailor me as desired
              {'fg':'black',      'bg':'beige'},      # or do PickBg/Fg chooser
              {'fg':'yellow',     'bg':'purple'},
              {'fg':'black',      'bg':'brown'},
              {'fg':'lightgreen', 'bg':'darkgreen'},
              {'fg':'darkblue',   'bg':'orange'},
              {'fg':'orange',     'bg':'darkblue'}]

    fonts  = [('courier',    9+FontScale, 'normal'),  # platform-neutral fonts
              ('courier',   12+FontScale, 'normal'),  # (family, size, style)
              ('courier',   10+FontScale, 'bold'),    # or popup a listbox
              ('courier',   10+FontScale, 'italic'),  # make bigger on linux
              ('times',     10+FontScale, 'normal'),
              ('helvetica', 10+FontScale, 'normal'),
              ('ariel',     10+FontScale, 'normal'),
              ('system',    10+FontScale, 'normal'),
              ('courier',   20+FontScale, 'normal')]

    def __init__(self, file=None):
        self.file = file

        self.openDialog = None
        self.saveDialog = None
        self.lastfind   = None
        self.current_parse = None
        self.mode = None
	self.last_save_signature = None
	self.last_compile_signature = None

        # First top-level window is Tk(); rest are Toplevel()
        global root
        if not root:
            root = Tk()
            self.top = root
        else:
            self.top = Toplevel(root)

        ccg2xml.late_init_graphics()
        openfiles[self] = True
        self.top.protocol('WM_DELETE_WINDOW', self.onClose)

        # We create an outer frame to hold the toolbar and the main widget.
        # Create all the different kinds of main widget.
        # FIXME: Maybe outer isn't necessary?
        self.outer = Frame(self.top)
        self.outer.pack(expand=YES, fill=BOTH)  # make frame stretchable
        self.modes = {}
        for mode in self.modelist:
            self.modes[mode] = self.modelist[mode](self.outer, self)
        self.main = None
        self.toolbar_widget = None
        self.checkbar_widget = None
        #self.switch_to('Edit')
        self.setFileName(None)
        if file:
        	self.onFirstOpen(file)
	else:
		# When the user has just opened a new file
		# Need to load template from the src folder
	        openccg_home = os.environ['OPENCCG_HOME']
		template = open(openccg_home + '/src/ccg2xml/grammar_template.ccg', 'r').read()
		self.setAllText(template)
	
	# Save the MD5 signature for future comparison
	self.last_save_signature = self.getSignature(self.getAllText())
    	#print "The modes should have been initialized now"
	self.switch_to('Edit')

    def switch_to(self, mode):
        # Switch to a different mode (display, edit, test).  Remove the
        # existing main and toolbar widgets, if existing.  Redo the menubar
        # and toolbar widgets according to the new mode and then display
        # the new widgets.
        #
        # FIXME: We should probably create the menubar and toolbar widgets
        # only once, and remember them.
        if self.mode != mode:
            if self.main:
                self.main.pack_forget()
            if self.toolbar_widget:
                self.toolbar_widget.pack_forget()
            if self.checkbar_widget:
                self.checkbar_widget.pack_forget()
            self.mode = mode
            self.main = self.modes[mode]
            self.makeMenubar()
            self.makeToolbar(mode)
            self.makeCheckbar()
	    #print "Reinit being called now... "
            self.main.reinit()
            # Pack the main widget after the toolbar, so it goes below it.
            self.main.pack(side=TOP, expand=YES, fill=BOTH)

    # Create the menubar; assumes that self.menubar has been set to the
    # appropriate menubar description.  Note that the menubar has to be a
    # child of the top-level window itself rather than any child of it, so
    # that it can be correctly displayed at the top of the window -- or
    # possibly in its decoration (Windows) or at top of screen (Mac).
    #
    # From PP2E guimaker.py.
    def makeMenubar(self):
        menubar = Menu(self.top)
        self.top.config(menu=menubar)

        for (name, key, items) in self.main.menubar:
            pulldown = Menu(menubar)
            self.addMenuItems(pulldown, items)
            menubar.add_cascade(label=name, underline=key, menu=pulldown)

        if sys.platform[:3] == 'win':
            menubar.add_command(label='Help', command=self.help)
        else:
            pulldown = Menu(menubar)  # linux needs real pulldown
            pulldown.add_command(label='About', command=self.help)
            menubar.add_cascade(label='Help', menu=pulldown)

    # Add items to a menu or submenu.  From PP2E guimaker.py.
    def addMenuItems(self, menu, items):
        for item in items:                     # scan nested items list
            if item == 'separator':            # string: add separator
                menu.add_separator({})
            elif type(item) is list:       # list: disabled item list
                for num in item:
                    menu.entryconfig(num, state=DISABLED)
            elif type(item[2]) is not list:
                menu.add_command(label     = item[0],         # command: 
                                 underline = item[1],         # add command
                                 command   = item[2])         # cmd=callable
            else:
                pullover = Menu(menu)
                self.addMenuItems(pullover, item[2])          # sublist:
                menu.add_cascade(label     = item[0],         # make submenu
                                 underline = item[1],         # add cascade
                                 menu      = pullover) 

    def makeToolbar(self, selected):
        """
        make toolbar (of buttons) at top, if any
        expand=no, fill=x so same width on resize
        """
        if self.main.toolbar:
            self.toolbar_widget = Frame(self.outer, cursor='hand2',
                                        relief=SUNKEN, bd=2)
            self.toolbar_widget.pack(side=TOP, fill=X)
            for (name, action, where) in self.main.toolbar:
                but = Button(self.toolbar_widget, text=name,
                             command=action)
                if name == selected:
                    but.config(relief=SUNKEN)
                but.pack(where)

    def makeCheckbar(self):
        """
        make check-button bar at top, if any
        expand=no, fill=x so same width on resize
        """
        if self.main.checkbar:
            self.checkbar_widget = Frame(self.outer, cursor='hand2',
                                         relief=SUNKEN, bd=2)
            self.checkbar_widget.pack(side=TOP, fill=X)
            for (name, var) in self.main.checkbar:
                Checkbutton(self.checkbar_widget, text=name,
                            variable=var,
                            command=self.main.redraw).pack(side=LEFT)

    def getAllText(self):
        return self.modes['Edit'].getAllText()

    def setAllText(self, text):
        self.modes['Edit'].setAllText(text)
        #self.modes['Display'].setAllText(text)

    def _getints(self, string):
        """Internal function."""
        if string:
            if type(string) is str:
                textwid = self.modes['Edit'].text
                return tuple(map(getint, textwid.tk.splitlist(string)))
            else:
                return string

    def edit(self, *args):
        """Internal method
        
        This method controls the undo mechanism and
        the modified flag. The exact behavior of the
        command depends on the option argument that
        follows the edit argument. The following forms
        of the command are currently supported:
        
        edit_modified, edit_redo, edit_reset, edit_separator
        and edit_undo
            
        """
        textwid = self.modes['Edit'].text
        return self._getints(
            textwid.tk.call((textwid._w, 'edit') + args)) or ()

    def edit_modified(self, arg=None):
        """Get or Set the modified flag

        If arg is not specified, returns the modified
        flag of the widget. The insert, delete, edit undo and
        edit redo commands or the user can set or clear the
        modified flag. If boolean is specified, sets the
        modified flag of the widget to arg.
        """

	# Added to use md5 functionality to watch for changed data
	if (arg == None):
		alltext = self.getAllText()
		if (self.last_save_signature != self.getSignature(alltext)):
			return YES
        return self.edit("modified", arg)

    def onInfo(self):
        text  = self.getAllText()                  # added on 5/3/00 in 15 mins
        bytes = len(text)                          # words uses a simple guess: 
        lines = len(string.split(text, '\n'))      # any separated by whitespace
        words = len(string.split(text)) 
        index = self.main.text.index(INSERT)
        where = tuple(string.split(index, '.'))

        showinfo('CCG Editor Information',
                 'Current location:\n\n' +
                 'line:\t%s\ncolumn:\t%s\n\n' % where +
                 'File text statistics:\n\n' +
                 'Modified: %s\n\n' % self.edit_modified()+
                 'bytes:\t%d\nlines:\t%d\nwords:\t%d\n' %
                 (bytes, lines, words))

    #######################
    # Search menu commands
    #######################
 
    def onGoto(self, line=None):
        if not line:
		line = askinteger('CCG Editor', 'Enter line number')
        self.main.text.update() 
        self.main.text.focus()
        if line is not None:
            maxindex = self.main.text.index(END+'-1c')
            maxline  = atoi(split(maxindex, '.')[0])
            if line > 0 and line <= maxline:
                self.main.text.mark_set(INSERT, '%d.0' % line)      # goto line
                self.main.text.tag_remove(SEL, '1.0', END)          # delete selects
                self.main.text.tag_add(SEL, INSERT, 'insert + 1l')  # select line
                self.main.text.see(INSERT)                          # scroll to line
            else:
                showerror('CCG Editor', 'Bad line number')

    def onFind(self, lastkey=None):
        key = lastkey or askstring('CCG Editor', 'Enter search string')
        self.main.text.update()
        self.main.text.focus()
        self.lastfind = key
        if key:
            where = self.main.text.search(key, INSERT, END)        # don't wrap
            if not where:
                showerror('CCG Editor', 'String not found')
            else:
                pastkey = where + '+%dc' % len(key)           # index past key
                self.main.text.tag_remove(SEL, '1.0', END)         # remove any sel
                self.main.text.tag_add(SEL, where, pastkey)        # select key 
                self.main.text.mark_set(INSERT, pastkey)           # for next find
                self.main.text.see(where)                          # scroll display

    def onRefind(self):
        self.onFind(self.lastfind)

    ######################
    # Tools menu commands 
    ######################

    def onFontList(self):
        self.fonts.append(self.fonts[0])           # pick next font in list
        del self.fonts[0]                          # resizes the text area
        self.modes['Edit'].text.config(font=self.fonts[0]) 
        self.modes['Display'].text.config(font=self.fonts[0]) 

    def onColorList(self):
        self.colors.append(self.colors[0])         # pick next color in list
        del self.colors[0]                         # move current to end
        self.modes['Edit'].text.config(fg=self.colors[0]['fg'], bg=self.colors[0]['bg']) 
        self.modes['Display'].text.config(fg=self.colors[0]['fg'], bg=self.colors[0]['bg']) 

    def onPickFg(self): 
        self.pickColor('fg')
    def onPickBg(self):
        self.pickColor('bg')
    def pickColor(self, part):
        (triple, hexstr) = askcolor()
        if hexstr:
            apply(self.modes['Edit'].text.config, (), {part: hexstr})
            apply(self.modes['Display'].text.config, (), {part: hexstr})

#     def onRunCode(self, parallelmode=1):
#         """
#         run Python code being edited--not an ide, but handy;
#         tries to run in file's dir, not cwd (may be pp2e root);
#         inputs and adds command-line arguments for script files;
#         code's stdin/out/err = editor's start window, if any;
#         but parallelmode uses start to open a dos box for i/o;
#         """
#         from PP2E.launchmodes import System, Start, Fork
#         filemode = 0
#         thefile  = str(self.getFileName())
#         cmdargs  = askstring('CCG Editor', 'Commandline arguments?') or ''
#         if os.path.exists(thefile):
#             filemode = askyesno('CCG Editor', 'Run from file?')
#         if not filemode:                                    # run text string
#             namespace = {'__name__': '__main__'}            # run as top-level
#             sys.argv = [thefile] + string.split(cmdargs)    # could use threads
#             exec self.getAllText() + '\n' in namespace      # exceptions ignored
#         elif askyesno('CCG Editor', 'Text saved in file?'):
#             mycwd = os.getcwd()                             # cwd may be root
#             os.chdir(os.path.dirname(thefile) or mycwd)     # cd for filenames
#             thecmd  = thefile + ' ' + cmdargs
#             if not parallelmode:                            # run as file
#                 System(thecmd, thecmd)()                    # block editor
#             else:
#                 if sys.platform[:3] == 'win':               # spawn in parallel
#                     Start(thecmd, thecmd)()                 # or use os.spawnv
#                 else:
#                     Fork(thecmd, thecmd)()                  # spawn in parallel
#             os.chdir(mycwd)

    #####################
    # File menu commands
    #####################

    def getSignature(self, contents):
    	return md5.md5(contents).digest()

    def my_askopenfilename(self):      # objects remember last result dir/file
        if not self.openDialog:
           self.openDialog = Open(initialdir=self.startfiledir, 
                                  filetypes=self.ftypes)
        return self.openDialog.show()

    def my_asksaveasfilename(self):    # objects remember last result dir/file
        if not self.saveDialog:
           self.saveDialog = SaveAs(initialdir=self.startfiledir, 
                                    filetypes=self.ftypes)
	self.last_save_signature = self.getSignature(self.getAllText())
        return self.saveDialog.show()
        
    def onOpen(self):
        file = self.my_askopenfilename()
        # FIXME! Only create new window if file exists and is readable
        if file:
            CFile(file)

    def onFirstOpen(self, file):
        try:
            text = open(file, 'r').read()
        except:
            showerror('CCG Editor', 'Could not open file ' + file)
        else:
            self.setAllText(text)
            self.setFileName(file)

    def compile_if_needed(self):
        # Compare the last compiled MD5 signature and present one
	# and compile if needed. 
	# To force compilation, set this signature to None
        text = self.getAllText()
	textSign = self.getSignature(text)
        if textSign != self.last_compile_signature:
		# Now compile
            	ccg2xml.init_global_state(errors_to_string=True)
            	ccg2xml.options.quiet = True
            	self.curparse = ccg2xml.parse_string(text)
            	self.last_compiled_signature = textSign

    def onDisplay(self):
        self.switch_to('Display')

    def onEdit(self):
        self.switch_to('Edit')

    def onLexicon(self):
        self.switch_to('Lexicon')

    def onTestbed(self):
        self.switch_to('Testbed')

    def onRules(self):
        self.switch_to('Rules')

    def onWords(self):
        self.switch_to('Words')

    def onFeatures(self):
        self.switch_to('Features')

    def onWords(self):
        self.switch_to('Words')

    def onNew(self):
        CFile()

    def getFileName(self):
        return self.currfile

    def setFileName(self, name):
        self.currfile = name  # for save
        if name:
            title = 'VisCCG Editor: %s' % name
        else:
            title = 'VisCCG Editor'
        self.top.title(title)
        self.top.iconname(title)

    def help(self):
        showinfo('Help', 'Sorry, no help for ' + self.__class__.__name__)

    # Close this window; if this is the last window, quit
    def onClose(self):
        assert self in openfiles
        if len(openfiles) == 1 or self.top == root:
            self.onQuit()
            # If we got this far, the user refused to quit, so do nothing
        else:
            ccg2xml.debug("fooooo\n")
            del openfiles[self]
            self.top.destroy()
        
    def onQuit(self):
        modfiles = False
        for f in openfiles:
            if f.edit_modified() == YES:
                modfiles = True
                break
        if not modfiles or askyesno('CCG Editor', 'Files are modified, Really quit?'):
            self.top.quit()



def main():
    ccg2xml.parse_arguments(sys.argv[1:])
    ccg2xml.init_global_state_once()
    if ccg2xml.global_args and len(ccg2xml.global_args) > 0:
        # file name:
        fname = ccg2xml.global_args[0]
    else:
        fname = None

    CFile(fname)
    mainloop()

if __name__ == '__main__':                            # when run as a script
    main()
