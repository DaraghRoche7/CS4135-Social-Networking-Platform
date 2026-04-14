import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import matplotlib.patches as mpatches
from matplotlib.patches import FancyBboxPatch

fig, ax = plt.subplots(figsize=(22, 17))
ax.set_xlim(0, 22)
ax.set_ylim(0, 17)
ax.axis('off')
fig.patch.set_facecolor('#FAFAFA')

# ── Colour palette ──────────────────────────────────────────────────────────
C = {
    'entity':   {'header': '#2E4057', 'body': '#EEF2F7', 'text': '#FFFFFF', 'border': '#2E4057'},
    'vo':       {'header': '#1B6CA8', 'body': '#E8F4FD', 'text': '#FFFFFF', 'border': '#1B6CA8'},
    'agg':      {'header': '#7B2D8B', 'body': '#F5EBF8', 'text': '#FFFFFF', 'border': '#7B2D8B'},
    'repo':     {'header': '#1A7A4A', 'body': '#E8F5EE', 'text': '#FFFFFF', 'border': '#1A7A4A'},
    'service':  {'header': '#B85C00', 'body': '#FEF3E8', 'text': '#FFFFFF', 'border': '#B85C00'},
    'event':    {'header': '#C0392B', 'body': '#FDECEA', 'text': '#FFFFFF', 'border': '#C0392B'},
}

def draw_class(ax, x, y, w, kind, stereotype, name, fields, methods=[]):
    """Draw a UML class box at (x, y) with given width."""
    line_h = 0.32
    pad = 0.15
    col = C[kind]

    sections = [fields]
    if methods:
        sections.append(methods)

    # Calculate total height
    header_h = 0.70
    total_h = header_h + sum(pad + len(s) * line_h + pad for s in sections if s)
    total_h = max(total_h, header_h + 0.5)

    # Shadow
    shadow = FancyBboxPatch((x+0.07, y-total_h-0.07), w, total_h,
                             boxstyle="round,pad=0.04", linewidth=0,
                             facecolor='#CCCCCC', zorder=1)
    ax.add_patch(shadow)

    # Header
    header = FancyBboxPatch((x, y-header_h), w, header_h,
                             boxstyle="round,pad=0.04", linewidth=1.5,
                             edgecolor=col['border'], facecolor=col['header'], zorder=2)
    ax.add_patch(header)

    # Stereotype label
    ax.text(x + w/2, y - 0.25, f'«{stereotype}»',
            ha='center', va='center', fontsize=7.5, style='italic',
            color=col['text'], fontweight='normal', zorder=3)
    # Class name
    ax.text(x + w/2, y - 0.52, name,
            ha='center', va='center', fontsize=9.5, fontweight='bold',
            color=col['text'], zorder=3)

    # Body sections
    cur_y = y - header_h
    for i, section in enumerate(sections):
        if not section:
            continue
        sec_h = pad + len(section) * line_h + pad
        # Section background
        body = FancyBboxPatch((x, cur_y - sec_h), w, sec_h,
                               boxstyle="round,pad=0.02", linewidth=1.5,
                               edgecolor=col['border'], facecolor=col['body'], zorder=2)
        ax.add_patch(body)
        # Separator line between sections
        if i > 0:
            ax.plot([x+0.05, x+w-0.05], [cur_y, cur_y],
                    color=col['border'], linewidth=0.8, alpha=0.5, zorder=3)
        # Text rows
        for j, line in enumerate(section):
            ty = cur_y - pad - j * line_h - line_h/2
            is_method = line.strip().startswith('+') or line.strip().startswith('-') and '(' in line
            ax.text(x + 0.15, ty, line, ha='left', va='center',
                    fontsize=7.8, color='#222222',
                    fontfamily='monospace', zorder=3)
        cur_y -= sec_h

    return total_h

# ── Arrow helpers ────────────────────────────────────────────────────────────
def arrow(ax, x1, y1, x2, y2, style='dependency', label=''):
    kw = dict(arrowstyle='->', color='#444444', lw=1.4,
              connectionstyle='arc3,rad=0.0')
    if style == 'composition':
        kw['arrowstyle'] = '->'
        kw['color'] = '#2E4057'
        kw['lw'] = 1.8
    elif style == 'event':
        kw['color'] = '#C0392B'
        kw['linestyle'] = 'dashed'
    ax.annotate('', xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle=kw.pop('arrowstyle'),
                                color=kw['color'],
                                lw=kw.get('lw', 1.4),
                                linestyle=kw.get('linestyle', 'solid'),
                                connectionstyle=kw['connectionstyle']),
                zorder=4)
    if label:
        mx, my = (x1+x2)/2, (y1+y2)/2
        ax.text(mx+0.08, my+0.08, label, fontsize=7, color='#555555',
                style='italic', zorder=5)

# ════════════════════════════════════════════════════════════════════════════
# Box definitions  (x, y = TOP-LEFT corner)
# ════════════════════════════════════════════════════════════════════════════

# ── Note Entity (centre-top) ─────────────────────────────────────────────────
note_x, note_y, note_w = 7.8, 16.6, 6.2
note_fields = [
    '- id: UUID',
    '- title: String',
    '- content: String',
    '- moduleCode: String',
    '- uploadedBy: UUID',
    '- uploadedAt: DateTime',
    '- status: NoteStatus',
    '- fileName: String',
    '- fileSize: Long',
    '- downloadCount: Long',
]
note_methods = [
    '+ incrementDownloadCount(): void',
    '+ markAsDeleted(): void',
    '+ flag(): void',
    '+ isOwnedBy(userId: UUID): boolean',
    '+ addLike(userId: UUID): void',
    '+ removeLike(userId: UUID): void',
]
note_h = draw_class(ax, note_x, note_y, note_w, 'entity', 'Entity', 'Note',
                    note_fields, note_methods)

# ── NoteMetadata Value Object (left) ─────────────────────────────────────────
nm_x, nm_y, nm_w = 0.4, 13.5, 5.2
nm_h = draw_class(ax, nm_x, nm_y, nm_w, 'vo', 'Value Object', 'NoteMetadata',
    ['- moduleCode: String',
     '- tags: Set<String>',
     '- fileSize: Long',
     '- mimeType: String',
    ])

# ── NoteLike Value Object (right) ────────────────────────────────────────────
nl_x, nl_y, nl_w = 16.2, 13.5, 5.2
nl_h = draw_class(ax, nl_x, nl_y, nl_w, 'vo', 'Value Object', 'NoteLike',
    ['- userId: UUID',
     '- likedAt: DateTime',
    ])

# ── Note Aggregate (centre) ───────────────────────────────────────────────────
agg_x, agg_y, agg_w = 7.3, 10.4, 7.2
agg_h = draw_class(ax, agg_x, agg_y, agg_w, 'agg', 'Aggregate Root', 'Note Aggregate',
    ['[Note (root), NoteMetadata, Set<NoteLike>]'],
    ['+ enforceInvariants(): void'])

# ── NoteRepository (bottom-left) ─────────────────────────────────────────────
repo_x, repo_y, repo_w = 0.4, 6.8, 6.8
repo_h = draw_class(ax, repo_x, repo_y, repo_w, 'repo', 'Repository', 'NoteRepository',
    [],
    ['+ findById(id: UUID): Optional<Note>',
     '+ findByModule(code: String): List<Note>',
     '+ findByUploader(id: UUID): List<Note>',
     '+ save(note: Note): Note',
     '+ delete(id: UUID): void',
    ])

# ── NoteSharingService (bottom-right) ────────────────────────────────────────
svc_x, svc_y, svc_w = 14.8, 6.8, 6.8
svc_h = draw_class(ax, svc_x, svc_y, svc_w, 'service', 'Domain Service', 'NoteSharingService',
    [],
    ['+ shareNote(noteId: UUID,',
     '    targetUsers: List<UUID>): void',
     '+ validateOwnership(noteId: UUID,',
     '    userId: UUID): boolean',
    ])

# ── NoteUploaded Event (bottom-left-2) ───────────────────────────────────────
ev1_x, ev1_y, ev1_w = 0.4, 2.5, 5.8
ev1_h = draw_class(ax, ev1_x, ev1_y, ev1_w, 'event', 'Domain Event', 'NoteUploaded',
    ['- noteId: UUID',
     '- uploaderId: UUID',
     '- moduleCode: String',
     '- occurredAt: DateTime',
    ])

# ── NoteLiked Event ───────────────────────────────────────────────────────────
ev2_x, ev2_y, ev2_w = 7.8, 2.5, 5.2
ev2_h = draw_class(ax, ev2_x, ev2_y, ev2_w, 'event', 'Domain Event', 'NoteLiked',
    ['- noteId: UUID',
     '- userId: UUID',
     '- occurredAt: DateTime',
    ])

# ── NoteDeleted Event ─────────────────────────────────────────────────────────
ev3_x, ev3_y, ev3_w = 15.0, 2.5, 6.6
ev3_h = draw_class(ax, ev3_x, ev3_y, ev3_w, 'event', 'Domain Event', 'NoteDeleted',
    ['- noteId: UUID',
     '- uploaderId: UUID',
     '- title: String',
     '- moduleCode: String',
     '- occurredAt: DateTime',
    ])

# ════════════════════════════════════════════════════════════════════════════
# Relationships
# ════════════════════════════════════════════════════════════════════════════

# Aggregate → Note Entity (composition)
arrow(ax, agg_x + agg_w/2, agg_y,
          note_x + note_w/2, note_y - note_h,
          style='composition', label='contains (root)')

# Aggregate → NoteMetadata (composition)
arrow(ax, agg_x, agg_y - agg_h/2,
          nm_x + nm_w, nm_y - nm_h/2,
          style='composition', label='has')

# Aggregate → NoteLike (composition)
arrow(ax, agg_x + agg_w, agg_y - agg_h/2,
          nl_x, nl_y - nl_h/2,
          style='composition', label='has Set<>')

# Repository → Aggregate (dependency)
arrow(ax, repo_x + repo_w/2, repo_y,
          agg_x, agg_y - agg_h,
          label='manages')

# Domain Service → Aggregate (dependency)
arrow(ax, svc_x + svc_w/2, svc_y,
          agg_x + agg_w, agg_y - agg_h,
          label='uses')

# Note → NoteUploaded (event)
arrow(ax, note_x + 0.6, note_y - note_h,
          ev1_x + ev1_w/2, ev1_y,
          style='event', label='publishes')

# Note → NoteLiked (event)
arrow(ax, note_x + note_w/2, note_y - note_h,
          ev2_x + ev2_w/2, ev2_y,
          style='event', label='publishes')

# Note → NoteDeleted (event)
arrow(ax, note_x + note_w - 0.6, note_y - note_h,
          ev3_x + ev3_w/2, ev3_y,
          style='event', label='publishes')

# ════════════════════════════════════════════════════════════════════════════
# Legend
# ════════════════════════════════════════════════════════════════════════════
legend_items = [
    (C['entity']['header'],  '«Entity» — Aggregate root with identity & lifecycle'),
    (C['vo']['header'],      '«Value Object» — Immutable, no identity'),
    (C['agg']['header'],     '«Aggregate» — Consistency boundary'),
    (C['repo']['header'],    '«Repository» — Persistence abstraction'),
    (C['service']['header'], '«Domain Service» — Stateless domain logic'),
    (C['event']['header'],   '«Domain Event» — Async notification (RabbitMQ)'),
]

lx, ly = 0.3, 0.95
ax.text(lx, ly, 'Legend', fontsize=8.5, fontweight='bold', color='#333', va='top')
for i, (col, label) in enumerate(legend_items):
    bx = lx + (i % 3) * 7.2
    by = ly - 0.38 - (i // 3) * 0.38
    rect = FancyBboxPatch((bx, by - 0.22), 0.38, 0.22,
                           boxstyle="round,pad=0.03", facecolor=col, linewidth=0, zorder=3)
    ax.add_patch(rect)
    ax.text(bx + 0.48, by - 0.11, label, fontsize=7.2, va='center', color='#333', zorder=3)

# Title
ax.text(11, 16.85, 'Note Management — Domain Model Class Diagram',
        ha='center', va='center', fontsize=14, fontweight='bold', color='#1a1a2e', zorder=5)
ax.text(11, 16.55, 'StudyHub · CS4135 Software Architectures · Assignment 3',
        ha='center', va='center', fontsize=8.5, color='#666666', zorder=5)

plt.tight_layout(pad=0)
out = '/Users/mooneyfounas/Assignment 3/docs/bounded-contexts/note-management/class-diagram.png'
plt.savefig(out, dpi=180, bbox_inches='tight', facecolor='#FAFAFA')
print('Saved:', out)
