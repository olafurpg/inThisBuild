from recommonmark.parser import CommonMarkParser

source_parsers = {
    '.md': CommonMarkParser,
}

source_suffix = ['.rst', '.md']

html_theme = "alabaster"

html_theme_options = {
    'collapse_navigation': False,
    'display_version': False,
    'navigation_depth': 3,
}


master_doc = 'index'
project = u'Scalameta'
copyright = u'2014-2017, EPFL'
author = u'Eugene Burmako, Olafur Pall Geirsson'

html_logo = '_static/logo.png'
latex_documents = [
    (master_doc, 'Scalameta.tex', u'Scalameta Documentation',
     u'Eugene Burmako, Olafur Pall Geirsson', 'manual'),
]
