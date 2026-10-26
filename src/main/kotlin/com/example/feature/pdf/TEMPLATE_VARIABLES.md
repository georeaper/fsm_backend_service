# PDF Template Variables

Τα Field Report PDF templates χρησιμοποιούν σύνταξη Pebble.

Εμφάνιση μιας τιμής:

```html
<h1>Field Report {{ report.reportNumber }}</h1>
<p>Customer: {{ report.customerName }}</p>
```

Οι κενές ή `null` τιμές εμφανίζονται ως κενό κείμενο. Οι δυναμικές τιμές γίνονται αυτόματα HTML escaped.

## Main Report

| Variable | Περιγραφή |
|---|---|
| `report.id` | Field Report ID |
| `report.reportNumber` | Αριθμός αναφοράς |
| `report.title` | Τίτλος |
| `report.description` | Περιγραφή |
| `report.startDate` | Ημερομηνία έναρξης |
| `report.endDate` | Ημερομηνία λήξης |
| `report.scheduledStart` | Προγραμματισμένη έναρξη |
| `report.scheduledEnd` | Προγραμματισμένη λήξη |
| `report.status` | Κατάσταση |
| `report.priority` | Προτεραιότητα |
| `report.type` | Τύπος εργασίας |
| `report.department` | Τμήμα |
| `report.customerName` | Πελάτης |
| `report.clientName` | Όνομα υπογράφοντος πελάτη |
| `report.technicianName` | Τεχνικός |
| `report.contractName` | Συμβόλαιο |
| `report.ticketNumber` | Αριθμός Technical Case |
| `report.signatureDataUri` | Εικόνα υπογραφής πελάτη |

Παράδειγμα υπογραφής:

```html
{% if report.signatureDataUri %}
  <img src="{{ report.signatureDataUri }}" class="signature" alt="Client signature"/>
{% endif %}
```

## Tools

Η λίστα είναι διαθέσιμη ως `tools`.

Κάθε `tool` περιλαμβάνει:

- `tool.name`
- `tool.manufacturer`
- `tool.model`
- `tool.serialNumber`
- `tool.calibrationDate`

```html
{% for tool in tools %}
  <p>{{ tool.name }} — {{ tool.serialNumber }}</p>
{% else %}
  <p>No tools recorded</p>
{% endfor %}
```

## Equipment

Η λίστα είναι διαθέσιμη ως `equipments`.

Κάθε `equipment` περιλαμβάνει:

- `equipment.id`
- `equipment.equipmentId`
- `equipment.name`
- `equipment.manufacturer`
- `equipment.model`
- `equipment.serialNumber`
- `equipment.maintenanceName`
- `equipment.completed`
- `equipment.checkforms`

```html
{% for equipment in equipments %}
  <p>{{ equipment.name }} — {{ equipment.model }}</p>
{% else %}
  <p>No equipment recorded</p>
{% endfor %}
```

## Inventory / Spare Parts

Η λίστα είναι διαθέσιμη ως `inventories`.

Κάθε `item` περιλαμβάνει:

- `item.name`
- `item.quantity`

```html
{% for item in inventories %}
  <p>{{ item.name }} — Quantity: {{ item.quantity }}</p>
{% endfor %}
```

## CheckForms

Τα CheckForms βρίσκονται μέσα στο αντίστοιχο equipment:

- `check.description`
- `check.expected`
- `check.measured`
- `check.result`

```html
{% for equipment in equipments %}
  <h2>{{ equipment.name }}</h2>

  {% for check in equipment.checkforms %}
    <p>{{ check.description }}: {{ check.measured }}</p>
  {% endfor %}
{% endfor %}
```

## Company Header

Τα παρακάτω variables είναι διαθέσιμα σε όλα τα templates:

- `company.name`
- `company.email`
- `company.address`
- `company.phone`
- `company.footer`
- `company.logoDataUri`

## Βασικοί κανόνες

- Χρησιμοποίησε `{{ variable }}` για εμφάνιση τιμής.
- Χρησιμοποίησε `{% for item in items %}` για λίστες.
- Χρησιμοποίησε `{% if variable %}` για προαιρετικές τιμές.
- Μην χρησιμοποιείς JavaScript, external URLs, SVG, includes/imports ή Java method calls.
- Προτίμησε απλό HTML και tables. Τα CSS flexbox και grid δεν υποστηρίζονται αξιόπιστα στο PDF.
